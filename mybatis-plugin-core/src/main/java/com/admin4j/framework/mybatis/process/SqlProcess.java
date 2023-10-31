package com.admin4j.framework.mybatis.process;

import com.admin4j.framework.mybatis.interceptor.SqlInterceptor;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author andanyang
 * @since 2023/10/31 9:42
 */

public interface SqlProcess extends SqlInterceptor {

    @Override
    default String process(Statement statement) {

        // if (log.isDebugEnabled()) {
        //     // log.debug("SQL to parse, SQL: " + sql);
        // }
        if (statement instanceof Insert) {
            this.processInsert((Insert) statement);
        } else if (statement instanceof Select) {
            this.processSelect((Select) statement);
        } else if (statement instanceof Update) {
            this.processUpdate((Update) statement);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete) statement);
        }

        // if (log.isDebugEnabled()) {
        //     log.debug("parse the finished SQL: " + sql);
        // }
        return statement.toString();
    }

    /**
     * 新增
     */
    default void processInsert(Insert insert) {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除
     */
    default void processDelete(Delete delete) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新
     */
    default void processUpdate(Update update) {
        throw new UnsupportedOperationException();
    }

    /**
     * 查询
     */
    default void processSelect(Select select) {
        throw new UnsupportedOperationException();
    }
}
