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
 *
 * @author andanyang
 * @since 2023/3/28 17:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataScopeBO {

    /**
     * 数据表信息
     */
    private DataTableInfoDTO dataTableInfoDTO;

    /**
     * 权限类型。；1：本人；2：本部门；3本部门及下级部门；4自定义部门；5全部
     */
    private DataScopeEnum type;

    /**
     * 获取当前的用户ID
     */
    private Expression userId;


    /**
     * 当前用户属于哪个部门
     */
    private List<DeptInfoDTO> deptInfos;
    /**
     * 当前用户所管理的部门信息,
     * 有管理 管理的部门信息，将返回其以及下属的所有部门数据
     */
    private List<DeptInfoDTO> managerDeptInfos;
    /**
     * 自定义的部门ID
     */
    private List<DeptInfoDTO> customDeptInfos = Collections.emptyList();

    /**
     * @return 是否拥有管理部门
     */
    public boolean hasManagerDept() {
        return getManagerDeptInfos() != null && !getManagerDeptInfos().isEmpty();
    }

    public UserDataScopeBO(DataScopeEnum type) {
        this.type = type;
    }
}
