package com.admin4j.framework;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.SelectUtils;
import org.junit.Assert;
import org.junit.Test;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author andanyang
 * @since 2023/6/29 9:31
 */
public class TestSqlParser {

    @Test
    public void testSqlParser() throws JSQLParserException {

        // 根据sql创建select
        Select stmt = (Select) CCJSqlParserUtil.parse("SELECT col1 AS a, col2 AS b, col3 AS c FROM table T WHERE col1 = 10 AND col2 = 20 AND col3 = 30");

        Map<String, Expression> map = new HashMap<>();
        final Map<String, String> mapTable = new HashMap<>();

        ((PlainSelect) stmt.getSelectBody()).getFromItem().accept(new FromItemVisitorAdapter() {
            @Override
            public void visit(Table table) {
                // 获取别名 => 表名
                mapTable.put(table.getAlias().getName(), table.getName());
            }
        });

        ((PlainSelect) stmt.getSelectBody()).getWhere().accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(AndExpression expr) {
                // 获取where表达式
                System.out.println(expr);
            }
        });

        for (SelectItem selectItem : ((PlainSelect) stmt.getSelectBody()).getSelectItems()) {
            selectItem.accept(new SelectItemVisitorAdapter() {
                @Override
                public void visit(SelectExpressionItem item) {
                    // 获取字段别名 => 字段名
                    map.put(item.getAlias().getName(), item.getExpression());
                }
            });
        }

        System.out.println("map " + map);
        System.out.println("mapTables" + mapTable);
    }

    /**
     * 单表where条件拼装
     * 单表SQL查询
     *
     * @throws JSQLParserException
     */
    @Test
    public void testSelectOneTable() throws JSQLParserException {
        // 单表全量
        Table table = new Table("test");
        Select select = SelectUtils.buildSelectFromTable(table);
        System.err.println(select); // SELECT * FROM test

        // 指定列查询
        Select buildSelectFromTableAndExpressions = SelectUtils.buildSelectFromTableAndExpressions(new Table("test"), new Column("col1"), new Column("col2"));
        System.err.println(buildSelectFromTableAndExpressions); // SELECT col1, col2 FROM test

        // WHERE =
        EqualsTo equalsTo = new EqualsTo(); // 等于表达式
        equalsTo.setLeftExpression(new Column(table, "user_id")); // 设置表达式左边值
        equalsTo.setRightExpression(new StringValue("123456"));// 设置表达式右边值
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody(); // 转换为更细化的Select对象
        plainSelect.setWhere(equalsTo);
        System.err.println(plainSelect);//  SELECT * FROM test WHERE test.user_id = '123456'

        // WHERE  != <>
        NotEqualsTo notEqualsTo = new NotEqualsTo();
        notEqualsTo.setLeftExpression(new Column(table, "user_id")); // 设置表达式左边值
        notEqualsTo.setRightExpression(new StringValue("123456"));// 设置表达式右边值
        PlainSelect plainSelectNot = (PlainSelect) select.getSelectBody();
        plainSelectNot.setWhere(notEqualsTo);
        System.err.println(plainSelectNot);//  SELECT * FROM test WHERE test.user_id <> '123456'

        // 其他运算符, 参考上面代码添加表达式即可
        GreaterThan gt = new GreaterThan(); // ">"
        GreaterThanEquals geq = new GreaterThanEquals(); // ">="
        MinorThan mt = new MinorThan(); // "<"
        MinorThanEquals leq = new MinorThanEquals();// "<="
        IsNullExpression isNull = new IsNullExpression(); // "is null"
        isNull.setNot(true);// "is not null"
        LikeExpression nlike = new LikeExpression();
        nlike.setNot(true); // "not like"
        Between bt = new Between();
        bt.setNot(true);// "not between"

        // WHERE LIKE
        LikeExpression likeExpression = new LikeExpression(); // 创建Like表达式对象
        likeExpression.setLeftExpression(new Column("username")); // 表达式左边
        likeExpression.setRightExpression(new StringValue("张%")); // 右边表达式
        PlainSelect plainSelectLike = (PlainSelect) select.getSelectBody();
        plainSelectLike.setWhere(likeExpression);
        System.err.println(plainSelectLike); // SELECT * FROM test WHERE username LIKE '张%'

        // WHERE IN
        Set<String> deptIds = new HashSet<>(); // 创建IN范围的元素集合
        deptIds.add("0001");
        deptIds.add("0002");
        List<Expression> collect = deptIds.stream().map(StringValue::new).collect(Collectors.toList());
        ItemsList itemsList = new ExpressionList(collect); // 把集合转变为JSQLParser需要的元素列表
        InExpression inExpression = new InExpression(new Column("dept_id "), itemsList); // 创建IN表达式对象，传入列名及IN范围列表
        PlainSelect plainSelectIn = (PlainSelect) select.getSelectBody();
        plainSelectIn.setWhere(inExpression);
        System.err.println(plainSelectIn); // SELECT * FROM test WHERE dept_id  IN ('0001', '0002')

        // WHERE BETWEEN AND
        Between between = new Between();
        between.setBetweenExpressionStart(new LongValue(18)); // 设置起点值
        between.setBetweenExpressionEnd(new LongValue(30)); // 设置终点值
        between.setLeftExpression(new Column("age")); // 设置左边的表达式，一般为列
        PlainSelect plainSelectBetween = (PlainSelect) select.getSelectBody();
        plainSelectBetween.setWhere(between);
        System.err.println(plainSelectBetween); // SELECT * FROM test WHERE age BETWEEN 18 AND 30

        //  WHERE AND 多个条件结合,都需要成立
        AndExpression andExpression = new AndExpression(); // AND 表达式
        andExpression.setLeftExpression(equalsTo); // AND 左边表达式
        andExpression.setRightExpression(between);  // AND 右边表达式
        PlainSelect plainSelectAnd = (PlainSelect) select.getSelectBody();
        plainSelectAnd.setWhere(andExpression);
        System.err.println(plainSelectAnd); //  SELECT * FROM test WHERE test.user_id = '123456' AND age BETWEEN 18 AND 30

        //  WHERE OR 多个条件满足一个条件成立返回
        OrExpression orExpression = new OrExpression();// OR 表达式
        orExpression.setLeftExpression(equalsTo); // OR 左边表达式
        orExpression.setRightExpression(between);  // OR 右边表达式
        PlainSelect plainSelectOr = (PlainSelect) select.getSelectBody();
        plainSelectOr.setWhere(orExpression);
        System.err.println(plainSelectOr); // SELECT * FROM test WHERE test.user_id = '123456' OR age BETWEEN 18 AND 30

        // ORDER BY 排序
        OrderByElement orderByElement = new OrderByElement(); // 创建排序对象
        orderByElement.isAsc(); //  设置升序排列 从小到大
        orderByElement.setExpression(new Column("col01")); // 设置排序字段
        PlainSelect plainSelectOrderBy = (PlainSelect) select.getSelectBody();
        plainSelectOrderBy.addOrderByElements(orderByElement);
        System.err.println(plainSelectOrderBy); // SELECT * FROM test WHERE test.user_id = '123456' OR age BETWEEN 18 AND 30 ORDER BY col01
    }

    /**
     * JOIN 拼装
     * 多表SQL查询
     * JOIN / INNER JOIN: 如果表中有至少一个匹配，则返回行
     * LEFT JOIN: 即使右表中没有匹配，也从左表返回所有的行
     * RIGHT JOIN: 即使左表中没有匹配，也从右表返回所有的行
     * FULL JOIN: 只要其中一个表中存在匹配，就返回行
     */
    @Test
    public void testSelectManyTable() {
        Table t1 = new Table("tab1").withAlias(new Alias("t1").withUseAs(true)); // 表1
        Table t2 = new Table("tab2").withAlias(new Alias("t2", false)); // 表2
        PlainSelect plainSelect = new PlainSelect().addSelectItems(new AllColumns()).withFromItem(t1); // SELECT * FROM tab1 AS t1

        // JOIN ON 如果表中有至少一个匹配，则返回行
        Join join = new Join(); // 创建Join对象
        join.withRightItem(t2); // 添加Join的表 JOIN t2 =>JOIN tab2 t2
        EqualsTo equalsTo = new EqualsTo(); // 添加 = 条件表达式  t1.user_id  = t2.user_id
        equalsTo.setLeftExpression(new Column(t1, "user_id "));
        equalsTo.setRightExpression(new Column(t2, "user_id "));
        join.withOnExpression(equalsTo);// 添加ON
        plainSelect.addJoins(join);
        System.err.println(plainSelect); // SELECT * FROM tab1 AS t1 JOIN tab2 t2 ON t1.user_id  = t2.user_id

        // 设置join参数可实现其他类型join
        // join.setLeft(true); LEFT JOIN
        // join.setRight(true);  RIGHT JOIN
        // join.setFull(true); FULL JOIN
        // join.setInner(true);
    }


    @Test
    public void testSelect() throws JSQLParserException {

        String sqlStr = "select 1 from dual where a=b";

        Select statement = (Select) CCJSqlParserUtil.parse(sqlStr);
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        System.out.println("select = " + select);
        //select 字段
        List<SelectItem> selectItems = select.getSelectItems();

        selectItems.forEach(i -> System.out.println("i = " + i));
        Expression where = select.getWhere();
        System.out.println("where = " + where);
        FromItem fromItem = select.getFromItem();
        System.out.println("fromItem = " + fromItem);


        //select 字段
        SelectExpressionItem selectItem = (SelectExpressionItem) selectItems.get(0);
        Assert.assertEquals(new LongValue(1), selectItem.getExpression());

        //表名
        Table table = (Table) select.getFromItem();
        Assert.assertEquals("dual", table.getName());

        //where 条件
        EqualsTo equalsTo = (EqualsTo) select.getWhere();
        Column a = (Column) equalsTo.getLeftExpression();
        Column b = (Column) equalsTo.getRightExpression();
        Assert.assertEquals("a", a.getColumnName());
        Assert.assertEquals("b", b.getColumnName());

        //StatementVisitor
    }


}
