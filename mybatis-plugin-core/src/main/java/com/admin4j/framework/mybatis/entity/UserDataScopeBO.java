package com.admin4j.framework.mybatis.entity;


import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.Expression;

import java.util.Collections;
import java.util.List;

/**
 * 用户数据权限信息
 * @author andanyang
 * @since 2023/3/28 17:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataScopeBO {


    /**
     * 权限类型。；1：本人；2：本部门；3本部门及下级部门；4自定义部门；5全部
     */
    private DataScopeEnum type;

    /**
     * 模块
     */
    //private String module;
    /**
     * 自定义的部门ID
     */
    private List<Long> customDeptIds = Collections.emptyList();

    /**
     * 获取当前的用户ID
     */
    private Expression userId;
    /**
     * 当前用所属部门
     */
    private List<Long> deptIds;
    /**
     * 当前用所属部门 树
     */
    private List<String> deptTrees;
}
