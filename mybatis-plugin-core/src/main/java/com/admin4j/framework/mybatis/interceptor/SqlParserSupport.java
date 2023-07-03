package com.admin4j.framework.mybatis.interceptor;

import com.admin4j.framework.mybatis.exception.MybatisPluginException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author andanyang
 * @since 2023/7/3 10:39
 */
@Slf4j
public abstract class SqlParserSupport {


    /**
     * 解析sql
     *
     * @param sql
     * @return
     * @throws MybatisPluginException
     */
    public String parse(String sql) throws MybatisPluginException {
        if (log.isDebugEnabled()) {
            log.debug("original SQL: " + sql);
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            return processParser(statement,  sql);
        } catch (JSQLParserException e) {
            throw new MybatisPluginException(e.getCause(), "Failed to process, Error SQL: " + sql);
        }
    }

    /**
     * 执行 SQL 解析
     *
     * @param statement JsqlParser Statement
     * @return sql
     */
    protected String processParser(Statement statement,  String sql) {
        if (log.isDebugEnabled()) {
            //log.debug("SQL to parse, SQL: " + sql);
        }
        if (statement instanceof Insert) {
            this.processInsert((Insert) statement,  sql);
        } else if (statement instanceof Select) {
            this.processSelect((Select) statement,  sql);
        } else if (statement instanceof Update) {
            this.processUpdate((Update) statement,  sql);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete) statement,   sql);
        }
        sql = statement.toString();
        if (log.isDebugEnabled()) {
            log.debug("parse the finished SQL: " + sql);
        }
        return sql;
    }

    /**
     * 新增
     */
    protected void processInsert(Insert insert,  String sql) {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除
     */
    protected void processDelete(Delete delete,  String sql) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新
     */
    protected void processUpdate(Update update,  String sql) {
        throw new UnsupportedOperationException();
    }

    /**
     * 查询
     */
    protected void processSelect(Select select,  String sql) {
        throw new UnsupportedOperationException();
    }
}
