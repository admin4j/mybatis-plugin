package com.admin4j.framework.mybatis.test.mapper;

import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.test.entity.CrmCustom;
import com.admin4j.framework.mybatis.test.service.DataScopeInfoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author andanyang
 * @since 2023/7/3 13:30
 */
@SpringBootTest
public class CrmCustomMapperTest {

    @Resource
    CrmCustomMapper crmCustomMapper;
    @Autowired
    DataScopeInfoHandler dataScopeInfoHandler;

    // @BeforeAll
    public void setUp() throws Exception {
        // dataScopeInfoHandler.getUserDataScopeBO().setManagerDeptInfos(null);
    }

    private void doTest() {
        List<CrmCustom> crmCustoms = crmCustomMapper.query("客户");
        System.out.println("query = " + crmCustoms);

        List<Object> queryJoin = crmCustomMapper.queryJoin("客户");
        System.out.println("queryJoin = " + queryJoin);
        queryJoin = crmCustomMapper.queryLeftJoin("客户");
        System.out.println("queryLeftJoin = " + queryJoin);
    }

    @Test
    public void testQuery() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.ALL);
        doTest();
    }

    @Test
    public void testQuery_SELF() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.SELF);
        doTest();
    }

    @Test
    public void testQuery_SELF2() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.SELF);
        dataScopeInfoHandler.userDataScopeBO.setManagerDeptInfos(null);
        doTest();
    }

    @Test
    public void testQuery_DEPARTMENT() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.DEPARTMENT);
        doTest();
    }

    @Test
    public void testQuery_DEPARTMENT_SU() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.DEPARTMENT_SU);
        doTest();
    }

    @Test
    public void testQuery_CUSTOM_DEPARTMENT() {

        dataScopeInfoHandler.setDataScopeEnum(DataScopeEnum.CUSTOM_DEPARTMENT);
        doTest();
    }
}