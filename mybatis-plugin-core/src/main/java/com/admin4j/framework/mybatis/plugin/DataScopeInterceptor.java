package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.IDataScopeTableExpression;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.DataTableInfoDTO;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.NoDataException;
import com.admin4j.framework.mybatis.process.SelectSqlProcess;
import com.admin4j.framework.mybatis.util.MapperAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mybatis数据权限拦截器
 * https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
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

    public String process(String originSql, DataTableInfoDTO dataTableInfoDTO) throws JSQLParserException {

        UserDataScopeBO userDataScopeBO = dataScopeInfoService.currentDataScope(dataTableInfoDTO);
        userDataScopeBO.setDataTableInfoDTO(dataTableInfoDTO);
        userDataScopeThreadLocal.set(userDataScopeBO);

        String sql = process(originSql);
        processEnd();
        return sql;
    }

    protected Expression buildOriginTableExpression(final Table table) throws NoDataException {

        UserDataScopeBO userDataScopeBO = userDataScopeThreadLocal.get();
        Column aliasColumn = getAliasColumn(table, userDataScopeBO.getDataTableInfoDTO().getField());
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

        DataTableInfoDTO dataTableInfoDTO = userDataScopeThreadLocal.get().getDataTableInfoDTO();
        // 是资源表
        if (dataTableInfoDTO == null || !table.getName().equals(dataTableInfoDTO.getTable())) {
            return null;
        }

        return buildOriginTableExpression(table);
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

    private void clean() {
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
        UserDataScopeBO userDataScopeBO = dataScopeInfoService.currentDataScope(dataTableInfo);
        userDataScopeBO.setDataTableInfoDTO(dataTableInfo);
        userDataScopeThreadLocal.set(userDataScopeBO);

        return true;
    }

    private Map<String, DataTableInfoDTO> dataTableInfoDTOMap = new ConcurrentHashMap<>(64);
    private static final DataTableInfoDTO _EMPTY_DATA_TABLE = new DataTableInfoDTO();

    private DataTableInfoDTO getDataTableInfo(String msId) {

        return dataTableInfoDTOMap.computeIfAbsent(msId, (key) -> {

            DataScope annotation = null;
            try {
                annotation = MapperAnnotationUtil.getAnnotationByIdNoCache(msId, DataScope.class);
                if (annotation == null) {
                    return _EMPTY_DATA_TABLE;
                }
                DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
                dataTableInfoDTO.setModule(annotation.module());
                dataTableInfoDTO.setField(annotation.field());
                dataTableInfoDTO.setTable(annotation.table());
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
