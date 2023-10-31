package com.admin4j.framework.mybatis.interceptor;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author andanyang
 * @since 2023/10/31 9:06
 */
public interface SqlInterceptor {

    /**
     * 处理sql
     *
     * @param originSql 原始sql
     * @return 处理完之后的 sql
     */
    default String process(String originSql) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(originSql);
        return process(parse);
    }

    /**
     * 处理sql  Statement
     *
     * @param statement
     * @return
     */
    default String process(Statement statement) {

        processStatement(statement);
        String sql = statement.toString();
        processEnd();
        return sql;
    }

    void processStatement(Statement statement);

    /**
     * 结束处理
     * 清理资源
     */
    void processEnd();

    /**
     * 处理之前调用
     *
     * @param ms
     * @return false 不执行
     */
    default boolean processBefore(MappedStatement ms) {
        return true;
    }
}
