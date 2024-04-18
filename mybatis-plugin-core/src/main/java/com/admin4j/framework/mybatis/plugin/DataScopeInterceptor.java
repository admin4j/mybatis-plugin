package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.IDataScopeTableExpression;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.DataTableInfoDTO;
import com.admin4j.framework.mybatis.entity.PlainValue;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.NoDataException;
import com.admin4j.framework.mybatis.process.SelectSqlProcess;
import com.admin4j.framework.mybatis.util.MapperAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限拦截器
 *
 * @author andanyang
 * @since 2023/6/28 15:59
 */
@Slf4j
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE)
public class DataScopeInterceptor extends SelectSqlProcess {


    final IDataScopeInfoHandler dataScopeInfoService;
    final IDataScopeTableExpression dataScopeTableExpression;
    protected ThreadLocal<UserDataScopeBO> userDataScopeThreadLocal = new ThreadLocal<>();

    /**
     * 根据表信息，直接修改原始sql
     *
     * @param originSql
     * @param dataTableInfoDTO
     * @return
     * @throws JSQLParserException
     */
    public String process(String originSql, DataTableInfoDTO dataTableInfoDTO) throws JSQLParserException {

        UserDataScopeBO userDataScopeBO = dataScopeInfoService.currentDataScope(dataTableInfoDTO);
        userDataScopeBO.setDataTableInfoDTO(dataTableInfoDTO);
        userDataScopeThreadLocal.set(userDataScopeBO);

        String sql = process(originSql);
        processEnd();
        return sql;
    }

    public Expression buildOriginTableExpression(final Column aliasColumn) throws NoDataException {

        UserDataScopeBO userDataScopeBO = userDataScopeThreadLocal.get();
        DataScopeEnum type = userDataScopeBO.getType();
        switch (type) {
            case ALL:
                return dataScopeTableExpression.buildAll(aliasColumn, userDataScopeBO);
            case SELF:
                return dataScopeTableExpression.buildSelf(aliasColumn, userDataScopeBO);
            case DEPARTMENT:
                return dataScopeTableExpression.buildDepartment(aliasColumn, userDataScopeBO);
            // 拼接sql
            case DEPARTMENT_SU:
                return dataScopeTableExpression.buildDepartmentSub(aliasColumn, userDataScopeBO);
            case CUSTOM_DEPARTMENT:

                return dataScopeTableExpression.buildDepartmentCustom(aliasColumn, userDataScopeBO);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) throws NoDataException {

        UserDataScopeBO userDataScopeBO = userDataScopeThreadLocal.get();
        DataTableInfoDTO dataTableInfoDTO = userDataScopeBO.getDataTableInfoDTO();
        // 是资源表
        if (dataTableInfoDTO == null || !table.getName().equals(dataTableInfoDTO.getTable())) {
            return null;
        }

        // 列信息
        Column aliasColumn = getAliasColumn(table, userDataScopeBO.getDataTableInfoDTO().getField());
        Expression expression = buildOriginTableExpression(aliasColumn);
        if (expression == null) {
            return null;
        }
        if (StringUtils.isNotBlank(dataTableInfoDTO.getOrWhere())) {

            EqualsTo equalsTo = new EqualsTo();
            equalsTo.withLeftExpression(aliasColumn).withRightExpression(new PlainValue(dataTableInfoDTO.getOrWhere()));
            expression = new OrExpression(equalsTo, expression);
            expression = new Parenthesis(expression);
        }
        return expression;
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


    public void set(UserDataScopeBO userDataScopeBO) {
        userDataScopeThreadLocal.set(userDataScopeBO);
    }

    public void clean() {
        userDataScopeThreadLocal.remove();
    }

    /**
     * 处理之前调用
     *
     * @param ms
     * @return false 不执行
     */
    @Override
    public boolean processBefore(MappedStatement ms) {


        DataTableInfoDTO dataTableInfo = getDataTableInfo(ms.getId());
        if (_EMPTY_DATA_TABLE.equals(dataTableInfo)) {
            return false;
        }
        if (dataTableInfo.getTable().isEmpty()) {
            return false;
        }
        UserDataScopeBO userDataScopeBO = dataScopeInfoService.currentDataScope(dataTableInfo);
        userDataScopeBO.setDataTableInfoDTO(dataTableInfo);
        userDataScopeThreadLocal.set(userDataScopeBO);

        return true;
    }

    private Map<String, DataTableInfoDTO> dataTableInfoDTOMap = new ConcurrentHashMap<>(128);
    private static final DataTableInfoDTO _EMPTY_DATA_TABLE = new DataTableInfoDTO();

    private DataTableInfoDTO getDataTableInfo(String msId) {

        return dataTableInfoDTOMap.computeIfAbsent(msId, (key) -> {

            DataScope annotation = null;
            try {
                annotation = MapperAnnotationUtil.getAnnotationById(msId, DataScope.class);
                if (annotation == null) {
                    return _EMPTY_DATA_TABLE;
                }
                if (annotation.ignore()) {
                    return _EMPTY_DATA_TABLE;
                }
                DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
                dataTableInfoDTO.setModule(annotation.module());
                dataTableInfoDTO.setField(annotation.field());
                dataTableInfoDTO.setTable(annotation.table());
                dataTableInfoDTO.setOrWhere(annotation.orWhere());
                return dataTableInfoDTO;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void processEnd() {
        clean();
    }
}
