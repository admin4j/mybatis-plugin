package com.admin4j.framework.mybatis.process;

import com.admin4j.framework.mybatis.interceptor.SqlInterceptor;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

/**
 * 处理 select 语句
 *
 * @author andanyang
 * @since 2023/10/31 10:44
 */
public abstract class SelectSqlProcess extends AbstractSqlProcess implements SqlInterceptor {


    /**
     * 处理sql  Statement
     *
     * @param statement
     * @return
     */
    @Override
    public void processStatement(Statement statement) {
        if (statement instanceof Select) {
            processSelect((Select) statement);
        }
    }

    protected void processSelect(Select select) {

        // SelectBody是JSQLParser中的一个接口，可以理解为Select语句对象,在JSqlParser中，SelectBody接口提供了对Select语句的解析和操作功能。
        processSelectBody(select.getSelectBody(), null);
        // 处理 with 子语句
        List<WithItem> withItemsList = select.getWithItemsList();
        if (!ObjectUtils.isEmpty(withItemsList)) {
            withItemsList.forEach(withItem -> processSelectBody(withItem, null));
        }
    }

}
