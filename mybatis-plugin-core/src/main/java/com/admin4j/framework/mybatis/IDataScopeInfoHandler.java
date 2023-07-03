package com.admin4j.framework.mybatis;

import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;


/**
 * 当前用户数据权限信息获取回调
 *
 * @author andanyang
 * @since 2023/6/30 17:01
 */
public interface IDataScopeInfoHandler {


    /**
     * @return 返回当前数据权限信息
     */
    UserDataScopeBO currentDataScope(DataScope dataScope);
}
