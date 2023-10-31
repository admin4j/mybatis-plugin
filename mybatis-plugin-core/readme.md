# SelectBody 的十个实现类

## PlainSelect

PlainSelect类是JSQLParser中的一个重要类，用于表示SELECT语句的简单形式。它包含了查询的各个部分，如选择列表、FROM子句、WHERE子句等，并且提供了对这些部分的操作和解析功能。

以下是PlainSelect类的一些主要属性和方法：

1. 选择列表（SelectItems）：PlainSelect类中的选择列表用于指定查询结果中要显示的列。它可以通过addItem()
   方法添加一个Item对象，表示一个列名或表达式。
2. FROM子句（FromItem）：PlainSelect类中的FromItem对象表示查询中的表。可以通过setFromItem()方法设置FROM子句的内容。
3. WHERE子句（WhereClause）：PlainSelect类中的WhereClause对象表示查询中的WHERE条件。可以通过setWhereClause()方法设置WHERE子句的内容。
4. 排序规则（Sort规则）：PlainSelect类中的Sort规则用于指定查询结果的排序方式。可以通过setSort()方法设置排序规则。
5. 访问器方法：PlainSelect类提供了许多访问器方法，用于获取查询的各个部分的信息。例如，getSelectItems()
   方法返回选择列表，getFromItem()方法返回FROM子句对象，getWhereClause()方法返回WHERE子句对象等。
   6.解析和操作功能：PlainSelect类还提供了对查询的解析和操作功能。例如，通过解析查询字符串，可以将其转换为可遍历的层次结构，以便于对查询结果的处理。此外，PlainSelect类还提供了一些方法，用于对查询进行修改和优化，例如添加或删除查询中的元素，以及对查询的遍历等。

```java
        PlainSelect select=new PlainSelect();
        select.setSelect(new SelectExpression(new Column("column1"),"alias1"));
        select.setFrom(new Table("table1"));
        select.setWhere(new Equals(new Column("column2"),"column1"));
```

总之，PlainSelect类是JSQLParser中的一个重要类，用于表示SELECT语句的简单形式，并且提供了对这些部分的操作和解析功能。它可以帮助开发人员更好地理解和处理SELECT语句，提高应用程序的性能和灵活性。

## WithItem

WithItem是JSQLParser中的一个类，表示**WITH语句中的子查询**。
WITH语句是一种在SQL查询中创建临时表的语法，允许在查询中使用临时表，而无需将其存储在数据库中。WithItem类包含了一个子查询对象和该子查询的别名，开发人员可以使用WithItem类来处理WITH语句中的子查询。

WithItem类具有以下属性：

* `subSelect`：表示子查询的对象。
* `alias`：表示子查询的别名。

WithItem类还提供了一些方法，例如`accept()`，用于访问和操作子查询对象。

在使用JSQLParser解析和处理WITH语句时，开发人员可以创建WithItem对象，并将其添加到查询中。例如，下面是一个使用WithItem类的示例代码：

```
        //with 子语句
        String sql = "WITH myTable AS (SELECT * FROM table1) SELECT * FROM myTable";
         // 非with子语句
         //  String sql=" SELECT * FROM table1 t ,(select * from myTable) a";

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        // 获取 WITH 子句中的项
        WithItem withItem = (select).getWithItemsList().get(0);

        // 获取项的名称
        String name = withItem.getName();
        System.out.println("Name: " + name);

        // 获取项的列表
        SelectBody selectBody = withItem.getSubSelect().getSelectBody();
        System.out.println("With Item: " + selectBody.toString());
```

在上面的示例中，我们创建了一个WithItem对象，设置了子查询和别名，并将其添加到查询中。然后，我们可以使用SelectQuery对象执行查询操作。

总之，WithItem类是JSQLParser中用于表示WITH语句中的子查询的类。开发人员可以使用WithItem类来处理WITH语句中的子查询，并对其进行解析和操作。

## SetOperationList

SetOperationList 是 jSQLParser 框架中的一个类，用于表示 SQL 语句中的 SET 操作列表。

在 jSQLParser 中，SetOperationList 类继承自 SelectBody 类，表示一个 SELECT 语句中的多个 SET 操作的集合。一个 SET 操作由一个
SelectBody 和一个 SetOperation 类型组成，常见的 SetOperation 类型有 UNION、INTERSECT 和 EXCEPT。

SetOperationList 类有以下属性和方法：

- plainSelect: 表示 SET 操作列表中的第一个 SELECT 语句。
- selectOperationList: 表示 SET 操作列表中的其他 SELECT 语句和对应的 SetOperation 类型。

SetOperationList 类有以下常用的方法：

- getPlainSelect(): 返回 SET 操作列表中的第一个 SELECT 语句。
- setPlainSelect(PlainSelect plainSelect): 设置 SET 操作列表中的第一个 SELECT 语句。
- getSelects(): 返回 SET 操作列表中的所有 SELECT 语句和对应的 SetOperation 类型。

以下是一个示例代码，演示如何使用 SetOperationList 类：

```
    public void testSetOperationListExample() throws JSQLParserException {

        String sql = "SELECT * FROM table1 UNION SELECT * FROM table2 INTERSECT SELECT * FROM table3";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        SelectBody selectBody = select.getSelectBody();

        // 获取 SET 操作列表
        SetOperationList setOperationList = (SetOperationList) selectBody;

        // 获取第一个 SELECT 语句
        PlainSelect plainSelect = (PlainSelect) setOperationList.getSelects().get(0);
        System.out.println("First Select: " + plainSelect.toString());

        // 获取其他 SELECT 语句和对应的 SetOperation 类型
        List<SelectBody> selects = setOperationList.getSelects();
        for (int i = 1; i < selects.size(); i++) {
            selectBody = selects.get(i);
            SetOperation setOperation = setOperationList.getOperations().get(i - 1);
            System.out.println("Select: " + selectBody.toString());
            System.out.println("Set Operation: " + setOperation.toString());
        }
    }
```

以上代码解析了一个包含多个 SET 操作的 SELECT 语句，并获取了 SET 操作列表中的第一个 SELECT 语句和其他 SELECT 语句以及对应的
SetOperation 类型。输出结果如下：

```
First Select: SELECT * FROM table1
Select: SELECT * FROM table2
Set Operation: UNION
Select: SELECT * FROM table3
Set Operation: INTERSECT
```

这个示例展示了如何使用 SetOperationList 类来处理 SELECT 语句中的 SET 操作列表。你可以根据自己的需求，使用 jSQLParser
框架进行更复杂的 SQL 解析和处理。

## ValuesStatement 不常用