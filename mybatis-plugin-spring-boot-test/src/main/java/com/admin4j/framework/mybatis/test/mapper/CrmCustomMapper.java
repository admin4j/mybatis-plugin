package com.admin4j.framework.mybatis.test.mapper;

import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.SqlIn;
import com.admin4j.framework.mybatis.test.entity.CrmCustom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author andanyang
 * @since 2023/7/3 13:25
 */
@Mapper
@DataScope(module = "Custom", table = "crm_custom", field = "salesman")

public interface CrmCustomMapper {

    @DataScope(module = "Custom", table = "crm_custom", field = "salesman")
    List<CrmCustom> query(String name);

    List<Object> queryJoin(String name);

    List<Object> queryLeftJoin(String an);

    @SqlIn(mainTable = "p_purchase_order", subTable = "p_purchase_order_user", subField = "order_id")
    @DataScope(table = "p_purchase_order_user", field = "user_id")
    List<CrmCustom> querySys(String name);
}
