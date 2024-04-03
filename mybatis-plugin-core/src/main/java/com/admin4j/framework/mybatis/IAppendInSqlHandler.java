package com.admin4j.framework.mybatis;

import com.admin4j.framework.mybatis.constant.SqlIn;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * @author andanyang
 * @since 2024/4/2 17:47
 */
public interface IAppendInSqlHandler {
    /**
     * 返回 in 内的 子查询
     *
     * @param table
     * @return null: 不拼接
     */
    PlainSelect getSelectSql(Table table, SqlIn sqlIn);
}
