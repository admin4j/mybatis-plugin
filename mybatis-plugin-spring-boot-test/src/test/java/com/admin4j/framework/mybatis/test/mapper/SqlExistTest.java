package com.admin4j.framework.mybatis.test.mapper;

import com.admin4j.framework.mybatis.plugin.SqlExistInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

/**
 * @author andanyang
 * @since 2024/4/2 17:59
 */

@SpringBootTest
public class SqlExistTest {

    @Autowired
    SqlExistInterceptor SqlExistInterceptor;


    @BeforeEach
    public void setUp() throws Exception {
        // SqlExistInterceptor.
    }

    public void testSql(String originSql, String okSql) throws Exception {


        String targetSql = SqlExistInterceptor.process(originSql);
        System.out.println("targetSql = " + targetSql);
        assert Objects.equals(targetSql, okSql);
    }

    @Test
    public void testSqlExist() throws Exception {

        String originSql = "SELECT * FROM p_purchase_order ppo";

        testSql(originSql, "SELECT * FROM p_purchase_order ppo WHERE EXISTS (SELECT 1 FROM p_purchase_order_user WHERE ppo.id = p_purchase_order_user.order_id)");
        originSql = "SELECT * FROM p_purchase_order1 ppo";

        testSql(originSql, "SELECT * FROM p_purchase_order1 ppo");
        // originSql = "SELECT * FROM p_purchase_order ppo";

        // testSql(originSql, "SELECT * FROM p_purchase_order ppo WHERE EXISTS (SELECT 1 FROM p_purchase_order_user WHERE ppo.id = p_purchase_order_user.order_id)");
    }
}
