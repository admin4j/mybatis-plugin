package com.admin4j.framework.mybatis.mapper;

import com.admin4j.framework.mybatis.entity.CrmCustom;
import org.junit.jupiter.api.Test;
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

    @Test
    public void testQuery() {
        List<CrmCustom> crmCustoms = crmCustomMapper.query(null);
        System.out.println("query = " + crmCustoms);
    }
}