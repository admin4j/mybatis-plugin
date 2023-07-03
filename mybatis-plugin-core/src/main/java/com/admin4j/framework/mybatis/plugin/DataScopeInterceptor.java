package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.DeptInfoDTO;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.NoDataException;
import com.admin4j.framework.mybatis.interceptor.BaseInterceptor;
import com.admin4j.framework.mybatis.interceptor.PlainValue;
import com.admin4j.framework.mybatis.util.MapperAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

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
        sql = parse(sql);
        // 利用反射修改boundSql的字段
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, sql);

        // 修改完继续执行
        List<Object> query = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        clean();
        return query;

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
        UserDataScopeBO userDataScopeBO = userDataScopeThreadLocal.get();
        DataScopeEnum type = userDataScopeBO.getType();
        PlainValue plainValue = null;
        boolean isFirst = true;
        StringBuilder sql = null;
        switch (type) {
            case ALL:
                return null;
            case SELF:
                sql = new StringBuilder("select user_id from sys_user_dept where ");

                if (!userDataScopeBO.hasManagerDept()) {
                    return new EqualsTo(getAliasColumn(table, field), userDataScopeBO.getUserId());
                }

                //加上自己管理的部门
                //处理子部门数据
                isFirst = true;
                for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
                    if (!isFirst) {
                        sql.append(" OR ");
                    }
                    sql.append("dept_tree like '");
                    sql.append(dept.getDeptTree());
                    sql.append("%'");
                    isFirst = false;
                }
                ItemsList itemsList = new ExpressionList(new PlainValue(sql.toString()));
                return new InExpression(getAliasColumn(table, field), itemsList);
            case DEPARTMENT:
                // user_id in (select user_id from sys_user_dept where dept_id in (1,2))
                if (userDataScopeBO.getDeptInfos() == null || userDataScopeBO.getDeptInfos().isEmpty()) {
                    throw new NoDataException("no any dept id");
                }

                List<DeptInfoDTO> deptInfos = userDataScopeBO.getDeptInfos();
                //去除重复部门
                if (userDataScopeBO.hasManagerDept()) {
                    deptInfos = userDataScopeBO.getDeptInfos().stream().filter(i -> {
                        for (DeptInfoDTO info : userDataScopeBO.getManagerDeptInfos()) {
                            if (info.getDeptId().equals(i.getDeptId())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
                }

                boolean needOr = false;
                sql = new StringBuilder("select user_id from sys_user_dept where ");
                if (deptInfos.size() == 1) {
                    needOr = true;
                    sql.append("dept_id = " + deptInfos.get(0).getDeptId());
                } else if (deptInfos.size() > 1) {
                    needOr = true;
                    isFirst = true;

                    for (DeptInfoDTO dept : deptInfos) {
                        if (!isFirst) {
                            sql.append(" OR ");
                        }
                        sql.append("dept_id = ");
                        sql.append(dept.getDeptId());
                        isFirst = false;
                    }
                }

                //又有管理的部门，需要权限穿透
                if (userDataScopeBO.hasManagerDept()) {
                    //处理子部门数据
                    if (needOr) {
                        sql.append(" OR ");
                    }
                    isFirst = true;
                    for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
                        if (!isFirst) {
                            sql.append(" OR ");
                        }
                        sql.append("dept_tree like '");
                        sql.append(dept.getDeptTree());
                        sql.append("%'");
                        isFirst = false;
                    }
                }
                itemsList = new ExpressionList(new PlainValue(sql.toString()));
                return new InExpression(getAliasColumn(table, field), itemsList);
            //拼接sql
            case DEPARTMENT_SU:

                // user_id in (select user_id from sys_data_scope where dept_tree like '1,2,3,%' or dept_tree like '1,2,4,%')
                if (userDataScopeBO.getDeptInfos() == null || userDataScopeBO.getDeptInfos().isEmpty()) {
                    throw new NoDataException("no any dep tree");
                }

                plainValue = null;

                deptInfos = userDataScopeBO.getDeptInfos();
                if (userDataScopeBO.hasManagerDept()) {
                    //合并数组
                    deptInfos = (List<DeptInfoDTO>) CollectionUtils.union(deptInfos, userDataScopeBO.getManagerDeptInfos());
                }

                isFirst = true;
                sql = new StringBuilder("select user_id from sys_user_dept where ");
                for (DeptInfoDTO deptInfo : deptInfos) {
                    if (!isFirst) {
                        sql.append(" OR ");
                    }
                    sql.append("dept_tree like '");
                    sql.append(deptInfo.getDeptTree());
                    sql.append("%'");
                    isFirst = false;
                }
                plainValue = new PlainValue(sql.toString());

                itemsList = new ExpressionList(plainValue);
                return new InExpression(getAliasColumn(table, field), itemsList);

            case CUSTOM_DEPARTMENT:

                // user_id in (select user_id from sys_data_scope where dept_id in (1,2,3,4)
                if (userDataScopeBO.getCustomDeptInfos() == null || userDataScopeBO.getCustomDeptInfos().isEmpty()) {
                    throw new NoDataException("no any CustomDeptIds");
                }

                deptInfos = userDataScopeBO.getCustomDeptInfos();
                //去除重复部门
                if (userDataScopeBO.hasManagerDept()) {
                    deptInfos = userDataScopeBO.getCustomDeptInfos().stream().filter(i -> {
                        for (DeptInfoDTO info : userDataScopeBO.getManagerDeptInfos()) {
                            if (info.getDeptId().equals(i.getDeptId())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
                }

                needOr = false;
                sql = new StringBuilder("select user_id from sys_user_dept where ");
                if (deptInfos.size() == 1) {
                    needOr = true;
                    sql.append("dept_id = " + deptInfos.get(0).getDeptId());
                } else if (deptInfos.size() > 1) {
                    needOr = true;
                    isFirst = true;

                    for (DeptInfoDTO dept : deptInfos) {
                        if (!isFirst) {
                            sql.append(" OR ");
                        }
                        sql.append("dept_id = ");
                        sql.append(dept.getDeptId());
                        isFirst = false;
                    }
                }

                //又有管理的部门，需要权限穿透
                if (userDataScopeBO.hasManagerDept()) {
                    //处理子部门数据
                    if (needOr) {
                        sql.append(" OR ");
                    }
                    isFirst = true;
                    for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
                        if (!isFirst) {
                            sql.append(" OR ");
                        }
                        sql.append("dept_tree like '");
                        sql.append(dept.getDeptTree());
                        sql.append("%'");
                        isFirst = false;
                    }
                }
                itemsList = new ExpressionList(new PlainValue(sql.toString()));
                return new InExpression(getAliasColumn(table, field), itemsList);

            default:
                throw new IllegalArgumentException();
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
}
