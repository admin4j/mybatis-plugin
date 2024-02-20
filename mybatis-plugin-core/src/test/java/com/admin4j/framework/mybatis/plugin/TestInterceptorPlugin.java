package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.IDataScopeTableExpression;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.DataTableInfoDTO;
import com.admin4j.framework.mybatis.entity.DeptInfoDTO;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.MybatisPluginException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author andanyang
 * @since 2023/6/29 14:43
 */
@DataScope(table = "sys_user")
public class TestInterceptorPlugin {

    String dataTable = "sys_user";
    String dataField = "user_id";
    Expression dataValue = new LongValue(1);

    DataScopeInterceptor interceptor;
    DataTableInfoDTO dataTableInfoDTO;
    // 用户自己的信息
    UserDataScopeBO userDataScopeBO;

    IDataScopeInfoHandler dataScopeInfoService;

    @Before
    public void init() {

        userDataScopeBO = new UserDataScopeBO();
        userDataScopeBO.setDataTableInfoDTO(dataTableInfoDTO);
        userDataScopeBO.setType(DataScopeEnum.SELF);
        // userDataScopeBO.setCustomDeptIds(Arrays.asList(1L, 2L, 101L));
        userDataScopeBO.setUserId(new LongValue(1L));
        userDataScopeBO.setDeptInfos(Arrays.asList(new DeptInfoDTO(168L, "0,1"),

                new DeptInfoDTO(168L, "0,1"),
                new DeptInfoDTO(169L, "0,2"),
                new DeptInfoDTO(170L, "0,3"),
                new DeptInfoDTO(171L, "0,4")
        ));

        IDataScopeInfoHandler iDataScopeInfoHandler = new IDataScopeInfoHandler() {

            /**
             * @param dataTableInfoDTO
             * @return 返回当前数据权限信息
             */
            @Override
            public UserDataScopeBO currentDataScope(DataTableInfoDTO dataTableInfoDTO) {

                return userDataScopeBO;
            }
        };
        IDataScopeTableExpression iDataScopeTableExpression = new IDataScopeTableExpression();
        interceptor = new DataScopeInterceptor(iDataScopeInfoHandler, iDataScopeTableExpression);

        dataTableInfoDTO = new DataTableInfoDTO();
        dataTableInfoDTO.setModule("org");
        dataTableInfoDTO.setTable(dataTable);
        dataTableInfoDTO.setField(dataField);
    }


    public String process(String originSql, DataTableInfoDTO dataTableInfoDTO) throws JSQLParserException {

        UserDataScopeBO userDataScopeBO = dataScopeInfoService.currentDataScope(dataTableInfoDTO);
        userDataScopeBO.setDataTableInfoDTO(dataTableInfoDTO);
        interceptor.userDataScopeThreadLocal.set(userDataScopeBO);

        String sql = interceptor.process(originSql);
        interceptor.processEnd();
        return sql;
    }

    public String testInterceptor(String sql) {

        System.out.println("========================= start =========================");

        String s = null;
        try {
            s = process(sql, dataTableInfoDTO);
        } catch (MybatisPluginException | JSQLParserException e) {
            throw new RuntimeException(e);
        }

        System.out.println("========================= end =========================");
        return s;
    }


    public void testInterceptor(String sql, String successSql) {

        System.out.println("========================= start =========================");

        String newSql = null;
        try {

            newSql = process(sql, dataTableInfoDTO);
        } catch (MybatisPluginException e) {
            throw new RuntimeException(e);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("originSql  = " + sql);
        System.out.println("newSql     = " + newSql);
        System.out.println("successSql = " + successSql);
        assert successSql.equals(newSql);

        System.out.println("========================= end =========================");
    }

    @Test
    public void testSimpleSql() throws JSQLParserException, MybatisPluginException {

        // String s = interceptor.processrSingle("SELECT * FROM sys_user a,sys_user_role b", "12 = 1");
        //
        // System.out.println("s = " + s);

        userDataScopeBO.setType(DataScopeEnum.SELF);

        testInterceptor("SELECT * FROM sys_user a",
                "SELECT * FROM sys_user a WHERE a.user_id = 1");


        testInterceptor("SELECT * FROM sys_user where name = 'and'",
                "SELECT * FROM sys_user WHERE name = 'and' AND user_id = 1");
        testInterceptor("SELECT * FROM sys_user where name = 'and' or  age = 12",
                "SELECT * FROM sys_user WHERE (name = 'and' OR age = 12) AND user_id = 1");
        testInterceptor("SELECT * FROM sys_user where (name = 'and' or  age = 12)",
                "SELECT * FROM sys_user WHERE (name = 'and' OR age = 12) AND user_id = 1");

        /**
         * not
         */
        testInterceptor("SELECT * FROM sys_user WHERE not (id = ? OR name = ?)",
                "SELECT * FROM sys_user WHERE NOT (id = ? OR name = ?) AND user_id = 1");

        testInterceptor("SELECT * FROM sys_user u WHERE not (u.id = ? OR u.name = ?)", "SELECT * FROM sys_user u WHERE NOT (u.id = ? OR u.name = ?) AND u.user_id = 1");

        userDataScopeBO.setType(DataScopeEnum.DEPARTMENT_SU);

        DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
        dataTableInfoDTO.setField("user_id");
        dataTableInfoDTO.setModule("custom");
        dataTableInfoDTO.setTable("crm_custom_user_r");

        UserDataScopeBO userDataScopeBO = interceptor.dataScopeInfoService.currentDataScope(dataTableInfoDTO);
        interceptor.set(userDataScopeBO);
        Expression expression = interceptor.buildOriginTableExpression(new Column("a.user_id"));
        System.out.println("expression = " + expression);
    }

    @Test
    public void selectSubSelectIn() throws MybatisPluginException {

        // IN
        //  testInterceptor("SELECT * FROM sys_user e WHERE e.id IN (select e1.id from entity1 e1 where e1.id = ?)", "SELECT * FROM sys_user e WHERE e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");
        testInterceptor("SELECT * FROM sys_user e WHERE e.id IN " +
                "(select e1.id from entity1 e1 where e1.id = ?) and e.id = ?", "SELECT * FROM sys_user e WHERE e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.id = ? AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e WHERE e.id IN " +
                "(select e1.id from entity1 e1 where e1.id = ?) or e.id = ?", "SELECT * FROM sys_user e WHERE (e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) OR e.id = ?) AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e WHERE e.id = ? and e.id IN " +
                "(select e1.id from entity1 e1 where e1.id = ?)", "SELECT * FROM sys_user e WHERE e.id = ? AND e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e WHERE e.id = ? and e.id IN " +
                "(select e1.id from entity1 e1 where e1.id = ?) and e.id = ?", "SELECT * FROM sys_user e WHERE e.id = ? AND e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.id = ? AND e.user_id = 1");
    }

    @Test
    public void selectSubSelectEq() throws MybatisPluginException {
        /* = */
        testInterceptor("SELECT * FROM entity e WHERE e.id = (select e1.id from sys_user e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id = (SELECT e1.id FROM sys_user e1 WHERE e1.id = ? AND e1.user_id = 1)"
        );
    }

    @Test
    public void selectSubSelectInnerNotEq() throws MybatisPluginException {
        /* inner not = */
        testInterceptor("SELECT * FROM sys_user e WHERE not (e.id = (select e1.id from entity1 e1 where e1.id = ?))",
                "SELECT * FROM sys_user e WHERE NOT (e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?)) AND e.user_id = 1"
        );

        testInterceptor("SELECT * FROM sys_user e WHERE not (e.id = (select e1.id from entity1 e1 where e1.id = ?) and e.id = ?)",
                "SELECT * FROM sys_user e WHERE NOT (e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.id = ?) AND e.user_id = 1");
        testInterceptor("SELECT * FROM entity1 e WHERE not (e.id = (select e1.id from sys_user e1 where e1.id = ?) and e.id = ?)",
                "SELECT * FROM entity1 e WHERE NOT (e.id = (SELECT e1.id FROM sys_user e1 WHERE e1.id = ? AND e1.user_id = 1) AND e.id = ?)");
    }

    @Test
    public void selectSubSelectExists() throws MybatisPluginException {
        /* EXISTS */
        testInterceptor("SELECT * FROM sys_user e WHERE EXISTS (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM sys_user e WHERE EXISTS (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");


        /* NOT EXISTS */
        testInterceptor("SELECT * FROM sys_user_1 e WHERE NOT EXISTS (select e1.id from sys_user e1 where e1.id = ?)",
                "SELECT * FROM sys_user_1 e WHERE NOT EXISTS (SELECT e1.id FROM sys_user e1 WHERE e1.id = ? AND e1.user_id = 1)");
    }

    @Test
    public void selectSubSelect() throws MybatisPluginException {
        /* >= */
        testInterceptor("SELECT * FROM sys_user e WHERE e.id >= (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM sys_user e WHERE e.id >= (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");


        /* <= */
        testInterceptor("SELECT * FROM sys_user e WHERE e.id <= (select e1.id from entity1 e1 where e1.id = ?)", "SELECT * FROM sys_user e WHERE e.id <= (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");


        /* <> */
        testInterceptor("SELECT * FROM sys_user e WHERE e.id <> (select e1.id from entity1 e1 where e1.id = ?)", "SELECT * FROM sys_user e WHERE e.id <> (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1");
    }

    @Test
    public void selectFromSelect() throws MybatisPluginException {
        testInterceptor("SELECT * FROM (select e.id from sys_user e WHERE e.id = (select e1.id from entity1 e1 where e1.id = ?))",
                "SELECT * FROM (SELECT e.id FROM sys_user e WHERE e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ?) AND e.user_id = 1)");
    }

    @Test
    public void selectBodySubSelect() throws MybatisPluginException {
        testInterceptor("select t1.col1,(select t2.col2 from sys_user t2 where t1.col1=t2.col1) from sys_user t1",
                "SELECT t1.col1, (SELECT t2.col2 FROM sys_user t2 WHERE t1.col1 = t2.col1 AND t2.user_id = 1) FROM sys_user t1 WHERE t1.user_id = 1");
    }

    @Test
    public void selectLeftJoin() throws MybatisPluginException {
        // left join
        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id WHERE (e.id = ? OR e.name = ?) AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id WHERE (e.id = ? OR e.name = ?) AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id LEFT JOIN entity2 e2 ON e1.id = e2.id WHERE e.user_id = 1");
    }

    @Test
    public void selectRightJoin() throws MybatisPluginException {
        // right join
        testInterceptor("SELECT * FROM sys_user e right join entity1 e1 on e1.id = e.id",
                "SELECT * FROM sys_user e RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1");

        testInterceptor("SELECT * FROM with_as_1 e right join sys_user e1 on e1.id = e.id",
                "SELECT * FROM with_as_1 e RIGHT JOIN sys_user e1 ON e1.id = e.id WHERE e1.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e right join entity1 e1 on e1.id = e.id WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM sys_user e RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1 WHERE e.id = ? OR e.name = ?");

        testInterceptor("SELECT * FROM sys_user e right join entity1 e1 on e1.id = e.id right join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM sys_user e RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1 RIGHT JOIN entity2 e2 ON e1.id = e2.id");
    }

    @Test
    public void selectMixJoin() {
        testInterceptor("SELECT * FROM sys_user e right join entity1 e1 on e1.id = e.id left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM sys_user e RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1 LEFT JOIN entity2 e2 ON e1.id = e2.id");

        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id right join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id inner join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id INNER JOIN entity2 e2 ON e1.id = e2.id AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e left join entity1 e1 on e1.id = e.id inner join entity2 e2 on e1.id = e2.id where e1.id = 1",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 ON e1.id = e.id INNER JOIN entity2 e2 ON e1.id = e2.id AND e.user_id = 1 WHERE e1.id = 1");
    }


    @Test
    public void selectJoinSubSelect() {
        testInterceptor("select * from (select * from sys_user e) e1 " +
                        "left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM (SELECT * FROM sys_user e WHERE e.user_id = 1) e1 LEFT JOIN entity2 e2 ON e1.id = e2.id");

        testInterceptor("select * from sys_user e1 " +
                        "left join (select * from entity2 e2) e22 " +
                        "on e1.id = e22.id",
                "SELECT * FROM sys_user e1 LEFT JOIN (SELECT * FROM entity2 e2) e22 ON e1.id = e22.id WHERE e1.user_id = 1");
    }

    @Test
    public void selectSubJoin() {

        testInterceptor("select * FROM " +
                        "(sys_user e1 right JOIN entity2 e2 ON e1.id = e2.id)",
                "SELECT * FROM (sys_user e1 RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.user_id = 1)");

        testInterceptor("select * FROM " +
                        "(sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id)",
                "SELECT * FROM (sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) WHERE e1.user_id = 1");


        testInterceptor("select * FROM " +
                        "(sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) " +
                        "right join entity3 e3 on e1.id = e3.id",
                "SELECT * FROM (sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) RIGHT JOIN entity3 e3 ON e1.id = e3.id AND e1.user_id = 1");


        testInterceptor("select * FROM entity e " +
                        "LEFT JOIN (sys_user e1 right join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e LEFT JOIN (sys_user e1 RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.user_id = 1) ON e.id = e2.id");

        testInterceptor("select * FROM entity e " +
                        "LEFT JOIN (sys_user e1 left join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e LEFT JOIN (sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) ON e.id = e2.id AND e1.user_id = 1");

        testInterceptor("select * FROM entity e " +
                        "RIGHT JOIN (sys_user e1 left join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e RIGHT JOIN (sys_user e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) ON e.id = e2.id WHERE e1.user_id = 1");
    }


    @Test
    public void selectLeftJoinMultipleTrailingOn() {
        // 多个 on 尾缀的
        testInterceptor("SELECT * FROM sys_user e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN entity2 e2 ON e2.id = e1.id " +
                        "ON e1.id = e.id " +
                        "WHERE (e.id = ? OR e.NAME = ?)",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 LEFT JOIN entity2 e2 ON e2.id = e1.id ON e1.id = e.id WHERE (e.id = ? OR e.NAME = ?) AND e.user_id = 1");

        testInterceptor("SELECT * FROM sys_user e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN with_as_A e2 ON e2.id = e1.id " +
                        "ON e1.id = e.id " +
                        "WHERE (e.id = ? OR e.NAME = ?)",
                "SELECT * FROM sys_user e LEFT JOIN entity1 e1 LEFT JOIN with_as_A e2 ON e2.id = e1.id ON e1.id = e.id WHERE (e.id = ? OR e.NAME = ?) AND e.user_id = 1");
    }

    @Test
    public void selectInnerJoin() {
        // inner join
        testInterceptor("SELECT * FROM sys_user e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM sys_user e INNER JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1 WHERE e.id = ? OR e.name = ?");

        testInterceptor("SELECT * FROM sys_user e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM sys_user e INNER JOIN entity1 e1 ON e1.id = e.id AND e.user_id = 1 WHERE (e.id = ? OR e.name = ?)");

        // 隐式内连接
        testInterceptor("SELECT * FROM sys_user e,entity1 e1 " +
                "WHERE e.id = e1.id", "SELECT * FROM sys_user e, entity1 e1 WHERE e.id = e1.id AND e.user_id = 1");

        // 隐式内连接
        testInterceptor("SELECT * FROM sys_user a, with_as_entity1 b " +
                        "WHERE a.id = b.id",
                "SELECT * FROM sys_user a, with_as_entity1 b WHERE a.id = b.id AND a.user_id = 1");

        testInterceptor("SELECT * FROM sys_user a, with_as_entity1 b " +
                        "WHERE a.id = b.id",
                "SELECT * FROM sys_user a, with_as_entity1 b WHERE a.id = b.id AND a.user_id = 1");

        // SubJoin with 隐式内连接
        testInterceptor("SELECT * FROM (sys_user e,entity1 e1) " +
                        "WHERE e.id = e1.id",
                "SELECT * FROM (sys_user e, entity1 e1) WHERE e.id = e1.id AND e.user_id = 1");

        testInterceptor("SELECT * FROM ((entity e,entity1 e1),sys_user e2) " +
                        "WHERE e.id = e1.id and e.id = e2.id",
                "SELECT * FROM ((entity e, entity1 e1), sys_user e2) WHERE e.id = e1.id AND e.id = e2.id AND e2.user_id = 1");

        testInterceptor("SELECT * FROM (sys_user e,(entity1 e1,entity2 e2)) " +
                        "WHERE e.id = e1.id and e.id = e2.id",
                "SELECT * FROM (sys_user e, (entity1 e1, entity2 e2)) WHERE e.id = e1.id AND e.id = e2.id AND e.user_id = 1");

        // 沙雕的括号写法
        testInterceptor("SELECT * FROM (((sys_user e,entity1 e1))) " +
                        "WHERE e.id = e1.id",
                "SELECT * FROM (((sys_user e, entity1 e1))) WHERE e.id = e1.id AND e.user_id = 1");

    }


    @Test
    public void testManagerDeptInfos() throws JSQLParserException, MybatisPluginException {

        // String s = interceptor.processrSingle("SELECT * FROM sys_user a,sys_user_role b", "12 = 1");
        //
        // System.out.println("s = " + s);

        userDataScopeBO.setType(DataScopeEnum.SELF);
        List<DeptInfoDTO> managerDeptInfos = new ArrayList<DeptInfoDTO>();
        DeptInfoDTO deptInfoDTO1 = new DeptInfoDTO();
        deptInfoDTO1.setDeptId(110L);
        deptInfoDTO1.setDeptTree("100,110,");
        managerDeptInfos.add(deptInfoDTO1);

        DeptInfoDTO deptInfoDTO2 = new DeptInfoDTO();
        deptInfoDTO2.setDeptId(120L);
        deptInfoDTO2.setDeptTree("100,120,");
        managerDeptInfos.add(deptInfoDTO2);

        userDataScopeBO.setManagerDeptInfos(managerDeptInfos);

        testInterceptor("SELECT * FROM sys_user a",
                "SELECT * FROM sys_user a WHERE a.user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");

        testInterceptor("SELECT * FROM sys_user where name = 'and'",
                "SELECT * FROM sys_user WHERE name = 'and' AND user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");
        testInterceptor("SELECT * FROM sys_user where name = 'and' or  age = 12",
                "SELECT * FROM sys_user WHERE (name = 'and' OR age = 12) AND user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");
        testInterceptor("SELECT * FROM sys_user where (name = 'and' or  age = 12)",
                "SELECT * FROM sys_user WHERE (name = 'and' OR age = 12) AND user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");

        /**
         * not
         */
        testInterceptor("SELECT * FROM sys_user WHERE not (id = ? OR name = ?)",
                "SELECT * FROM sys_user WHERE NOT (id = ? OR name = ?) AND user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");

        testInterceptor("SELECT * FROM sys_user u WHERE not (u.id = ? OR u.name = ?)",
                "SELECT * FROM sys_user u WHERE NOT (u.id = ? OR u.name = ?) AND u.user_id IN (select user_id from sys_user_dept where dept_tree like '100,110,%' OR dept_tree like '100,120,%')");
    }

}
