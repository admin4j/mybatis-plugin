package com.admin4j.framework.mybatis.test.service;

import com.admin4j.framework.mybatis.IAppendExistSqlHandler;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andanyang
 * @since 2024/4/2 17:51
 */
@Service
public class AppendExistSqlHandlerImpl implements IAppendExistSqlHandler {
    /**
     * 拦截到的主表
     *
     * @param table
     * @return
     */
    @Override
    public PlainSelect getSelectSql(Table table) {
        if (!table.getName().equals("p_purchase_order")) {
            return null;
        }


        PlainSelect selectInsideExists = new PlainSelect();

        List<SelectItem> selectExpressionItems = new ArrayList<SelectItem>() {
            private static final long serialVersionUID = -1697959555117339354L;

            {
                // 假设我们查询 id 列
                add(new SelectExpressionItem(new Column("1")));
            }
        };

        selectInsideExists.setSelectItems(selectExpressionItems);
        selectInsideExists.setFromItem(new Table("p_purchase_order_user")); // 设置子查询的 FROM 子句


        // 设置WHERE子句
        EqualsTo equalsTo1 = new EqualsTo(); // 等于表达式
        // 设置表达式左边值
        equalsTo1.setLeftExpression(new Column("ppo.id"));
        // 设置表达式右边值
        equalsTo1.setRightExpression(new Column("p_purchase_order_user.order_id"));


        EqualsTo equalsTo2 = new EqualsTo(); // 等于表达式
        // 设置表达式左边值
        equalsTo2.setLeftExpression(new Column("user_id_1"));
        // 设置表达式右边值
        equalsTo2.setRightExpression(new LongValue(1));

        EqualsTo equalsTo3 = new EqualsTo(); // 等于表达式
        // 设置表达式左边值
        equalsTo3.setLeftExpression(new Column("user_role"));
        // 设置表达式右边值
        equalsTo3.setRightExpression(new LongValue(1));


        AndExpression andExpression1 = new AndExpression();
        andExpression1.withLeftExpression(equalsTo1);
        andExpression1.withRightExpression(equalsTo2);

        AndExpression andExpression2 = new AndExpression();

        andExpression2.withLeftExpression(andExpression1);
        andExpression2.withRightExpression(equalsTo3);

        selectInsideExists.withWhere(andExpression2);


        return selectInsideExists;
        // String sql = "(SELECT 1 FROM p_purchase_order_user WHERE ppo.id = p_purchase_order_user.order_id and user_id = 1 and user_role in ('1','2','3'))";
        // return sql;
    }
}
