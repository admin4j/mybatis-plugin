package com.admin4j.framework.mybatis.util;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * @author andanyang
 * @since 2024/4/2 17:57
 */
public class SqlUtils {

    /**
     * 获取字段别名
     * <p>tenantId 或 tableAlias.tenantId</p>
     *
     * @param table 表对象
     * @return 字段
     */
    public static Column getAliasColumn(Table table, String field) {
        StringBuilder column = new StringBuilder();

        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(".");
        } else {
            column.append(table.getName()).append(".");
        }
        column.append(field);
        return new Column(column.toString());
    }
}
