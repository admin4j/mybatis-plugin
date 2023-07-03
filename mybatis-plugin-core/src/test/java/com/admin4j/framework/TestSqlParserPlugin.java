package com.admin4j.framework;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author andanyang
 * @since 2023/6/29 14:43
 */
public class TestSqlParserPlugin {

    String dataTable = "sys_user";
    String dataField = "user_id";
    Expression dataValue = new LongValue(1);




    @Test
    public void selectJoin() throws JSQLParserException {

        testSql("SELECT * FROM sys_user as a,sys_user_role b where user_name =1");
        testSql("SELECT * FROM sys_user_role b,sys_user where user_name =1");
        testSql("SELECT * FROM sys_user a inner join sys_user_role r on r.user_id = a.user_id where user_name =1");
        testSql("SELECT * FROM sys_user_role r inner join  sys_user a  on r.user_id = a.user_id");

    }

    public void findTable(String sql) throws JSQLParserException {

    }

    public void testSql(String sql) throws JSQLParserException {

        System.out.println("======================== start ====================================");
        System.out.println("origin sql = " + sql);
        Select statement = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) statement.getSelectBody();
        //Table fromItem = plainSelect.getFromItem(Table.class);
        //List<Join> joins = plainSelect.getJoins();
        //System.out.println("joins = " + joins);
        processMainTable(plainSelect);
        processSubTable(plainSelect);

        System.out.println("plainSelect = " + plainSelect);
    }

    //再where后面添加字段
    public void appendWhereField(PlainSelect plainSelect, Table table) {

        if (!table.getName().equals(dataTable)) {
            return;
        }


        Expression where = plainSelect.getWhere();

        EqualsTo userIdEqualsTo = new EqualsTo(); // 等于表达式
        userIdEqualsTo.setLeftExpression(getAliasColumn(table)); // 设置表达式左边值
        userIdEqualsTo.setRightExpression(dataValue);// 设置表达式右边值

        if (where == null) {

            plainSelect.setWhere(userIdEqualsTo);
            //System.err.println("parseSql=" + plainSelect);
        } else {

            AndExpression andExpression = new AndExpression(); // AND 表达式
            andExpression.setLeftExpression(where); // AND 左边表达式
            andExpression.setRightExpression(userIdEqualsTo);  // AND 右边表达式

            plainSelect.setWhere(andExpression);
            //System.err.println("parseSql=" + plainSelect);
        }
    }

    // 处理主表
    public void processMainTable(PlainSelect plainSelect) {
        Table table = plainSelect.getFromItem(Table.class);

        appendWhereField(plainSelect, table);
    }

    // 处理子表
    public void processSubTable(PlainSelect plainSelect) {

        List<Join> joins = plainSelect.getJoins();
        if (joins == null) {
            return;
        }
        for (Join join : joins) {
            Table rightItem = (Table) join.getRightItem();
            if (rightItem.getName().equals(dataTable)) {
                appendWhereField(plainSelect, rightItem);
            }
        }
    }

    /**
     * 租户字段别名设置
     * <p>tenantId 或 tableAlias.tenantId</p>
     *
     * @param table 表对象
     * @return 字段
     */
    protected Column getAliasColumn(Table table) {
        StringBuilder column = new StringBuilder();
        // todo 该起别名就要起别名,禁止修改此处逻辑
        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(".");
        }
        column.append("user_id");
        return new Column(column.toString());
    }

}
