package com.admin4j.framework.mybatis.test.mapper;

import com.admin4j.framework.mybatis.plugin.SqlInInterceptor;
import com.admin4j.framework.mybatis.test.entity.CrmCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author andanyang
 * @since 2024/4/2 17:59
 */

@SpringBootTest
public class SqlInTest {

    @Autowired
    SqlInInterceptor sqlInInterceptor;
    @Resource
    CrmCustomMapper crmCustomMapper;

    @BeforeEach
    public void setUp() throws Exception {
        // SqlExistInterceptor.
    }

    public void testSql(String originSql, String okSql) throws Exception {


        String targetSql = sqlInInterceptor.process(originSql);
        System.out.println("targetSql = " + targetSql);
        assert Objects.equals(targetSql, okSql);
    }

    @Test
    public void testSqlExist() throws Exception {

        // String originSql = "SELECT a.id FROM p_purchase_order a INNER JOIN p_purchase_order_item b ON a.id = b.purchase_order_id AND b.del_flag = 0 AND a.tenant_id = 1 AND b.tenant_id = 1 WHERE a.del_flag = 0 GROUP BY a.id ";
        //
        // testSql(originSql, "SELECT * FROM p_purchase_order ppo WHERE EXISTS (SELECT 1 FROM p_purchase_order_user WHERE ppo.id = p_purchase_order_user.order_id)");
        // originSql = "SELECT * FROM p_purchase_order1 ppo";
        //
        // testSql(originSql, "SELECT * FROM p_purchase_order1 ppo");
        // originSql = "SELECT * FROM p_purchase_order ppo";

        // testSql(originSql, "SELECT * FROM p_purchase_order ppo WHERE EXISTS (SELECT 1 FROM p_purchase_order_user WHERE ppo.id = p_purchase_order_user.order_id)");
    }

    @Test
    public void testSqlMapper() throws Exception {

        List<CrmCustom> query = crmCustomMapper.querySys("12");
        System.out.println("query = " + query);
    }
}
