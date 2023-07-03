package com.admin4j.framework.mybatis.service;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author andanyang
 * @since 2023/7/3 13:32
 */
@Service
public class DataScopeInfoHandler implements IDataScopeInfoHandler {

    /**
     * @return 返回当前数据权限信息
     */
    @Override
    public UserDataScopeBO currentDataScope(DataScope dataScope) {
        UserDataScopeBO UserDataScopeBO = new UserDataScopeBO();
        UserDataScopeBO.setType(DataScopeEnum.ALL);
        UserDataScopeBO.setCustomDeptIds(Arrays.asList(1L, 2L, 101L));
        UserDataScopeBO.setUserId(new LongValue(1L));
        UserDataScopeBO.setDeptIds(Arrays.asList(168L, 192L, 191L));
        UserDataScopeBO.setDeptTrees(Arrays.asList("1,2,3,4,5,6,", "1,2,3,4,5,4,", "1,2,3,4,5,98,"));
        return UserDataScopeBO;
    }
}
