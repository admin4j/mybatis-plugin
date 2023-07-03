package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.IDataScopeTableExpression;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.NoDataException;
import com.admin4j.framework.mybatis.interceptor.BaseInterceptor;
import com.admin4j.framework.mybatis.interceptor.PlainValue;
import com.admin4j.framework.mybatis.util.MapperAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * mybatis数据权限拦截器
 *
 * @author andanyang
 * @since 2023/6/28 15:59
 */

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
@Slf4j
@RequiredArgsConstructor
public class DataScopeInterceptor extends BaseInterceptor implements Interceptor {


    final IDataScopeInfoHandler dataScopeInfoService;
    final IDataScopeTableExpression IDataScopeTableExpression;
    protected ThreadLocal<DataScope> dataScopeThreadLocal = new ThreadLocal<>();
    protected ThreadLocal<UserDataScopeBO> userDataScopeThreadLocal = new ThreadLocal<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String id = ms.getId();

        // 方法的参数
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];

        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        if (sqlCommandType == SqlCommandType.UNKNOWN || sqlCommandType == SqlCommandType.FLUSH) {
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();

        CacheKey cacheKey;
        BoundSql boundSql;
        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }


        DataScope annotation = MapperAnnotationUtil.getAnnotationById(id, DataScope.class);
        if (annotation == null) {
            return invocation.proceed();
        }

        UserDataScopeBO UserDataScopeBO = dataScopeInfoService.currentDataScope(annotation);
        userDataScopeThreadLocal.set(UserDataScopeBO);
        dataScopeThreadLocal.set(annotation);

        // 获取到原查询sql语句
        String sql = boundSql.getSql();
        try {
            sql = parse(sql);
        } catch (NoDataException noDataException) {
            return Collections.emptyList();
        }

        // 利用反射修改boundSql的字段
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, sql);

        // 修改完继续执行
        clean();
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    private void clean() {
        dataScopeThreadLocal.remove();
        userDataScopeThreadLocal.remove();
    }


    @Override
    protected void processSelect(Select select, String sql) {

        processSelectBody(select.getSelectBody(), null);
        List<WithItem> withItemsList = select.getWithItemsList();
        if (!ObjectUtils.isEmpty(withItemsList)) {
            withItemsList.forEach(withItem -> processSelectBody(withItem, null));
        }
    }

    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) throws NoDataException {

        DataScope dataScope = dataScopeThreadLocal.get();
        if (dataScope == null || (dataScope != null && !table.getName().equals(dataScope.table()))) {
            return null;
        }
        String field = dataScope.field();

        if (StringUtils.isBlank(field)) {
            field = "user_id";
        }

        Column aliasColumn = getAliasColumn(table, field);
        UserDataScopeBO userDataScopeBO = userDataScopeThreadLocal.get();
        DataScopeEnum type = userDataScopeBO.getType();
        PlainValue plainValue = null;
        boolean isFirst = true;
        StringBuilder sql = null;
        switch (type) {
            case ALL:
                return IDataScopeTableExpression.buildAll(aliasColumn, userDataScopeBO);
            case SELF:
                return IDataScopeTableExpression.buildSelf(aliasColumn, userDataScopeBO);
            case DEPARTMENT:
                return IDataScopeTableExpression.buildDepartment(aliasColumn, userDataScopeBO);
            //拼接sql
            case DEPARTMENT_SU:
                return IDataScopeTableExpression.buildDepartmentSub(aliasColumn, userDataScopeBO);
            case CUSTOM_DEPARTMENT:

                return IDataScopeTableExpression.buildDepartmentCustom(aliasColumn, userDataScopeBO);
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * 租户字段别名设置
     * <p>tenantId 或 tableAlias.tenantId</p>
     *
     * @param table 表对象
     * @return 字段
     */
    protected Column getAliasColumn(Table table, String field) {
        StringBuilder column = new StringBuilder();

        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(".");
        }
        column.append(field);
        return new Column(column.toString());
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
}
