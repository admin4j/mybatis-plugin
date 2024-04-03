package com.admin4j.framework.mybatis.test.service;

import com.admin4j.framework.mybatis.IAppendInSqlHandler;
import com.admin4j.framework.mybatis.constant.SqlIn;
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
 * @since 2024/4/2 18:22
 */
@Service
public class SqlInHandler implements IAppendInSqlHandler {


    @Override
    public PlainSelect getSelectSql(Table table, SqlIn sqlIn) {


        // SELECT * FROM p_purchase_order WHERE del_flag = 0 AND id in (SELECT order_id FROM p_purchase_order_user WHERE  user_id IN (SELECT user_id FROM sys_user_dept WHERE dept_tree LIKE '0,100,%'  OR dept_tree LIKE '0,101,%' )) AND tenant_id = 1 LIMIT 50;
        //(SELECT order_id FROM p_purchase_order_user WHERE  user_id IN (SELECT user_id FROM sys_user_dept WHERE dept_tree LIKE '0,100,%'  OR dept_tree LIKE '0,101,%' ))


        PlainSelect selectInsideExists = new PlainSelect();

        // Table mainTable = new Table(sqlIn.mainTable());
        // Column mainField = new Column(mainTable, sqlIn.mainField());
        List<SelectItem> selectExpressionItems = new ArrayList<SelectItem>() {
            private static final long serialVersionUID = -1697959555117339354L;

            {
                // 假设我们查询 id 列
                add(new SelectExpressionItem(new Column(sqlIn.subField())));
            }
        };

        selectInsideExists.setSelectItems(selectExpressionItems);

        Table inTable = new Table(sqlIn.subTable());
        // 设置子查询的 FROM 子句
        selectInsideExists.setFromItem(inTable);


        // // 设置WHERE子句
        // EqualsTo equalsTo1 = new EqualsTo(); // 等于表达式
        // // 设置表达式左边值
        // equalsTo1.setLeftExpression(new Column(fromItem, "order_id"));
        // // 设置表达式右边值
        // equalsTo1.setRightExpression(new Column(table, udoCustomObj.getObjFieldId()));


        // selectInsideExists.withWhere(equalsTo1);
        // subSelect.setSelectBody(selectInsideExists);


        return selectInsideExists;
    }
}
