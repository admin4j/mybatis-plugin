package com.admin4j.framework.mybatis.mapper;

import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.entity.CrmCustom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author andanyang
 * @since 2023/7/3 13:25
 */
@Mapper
public interface CrmCustomMapper {

    @DataScope(module = "Custom", table = "crm_custom", field = "salesman")
    List<CrmCustom> query(String name);
}
