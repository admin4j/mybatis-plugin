package com.admin4j.framework.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author andanyang
 * @since 2023/7/3 13:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptInfoDTO implements Comparable<DeptInfoDTO> {
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门树
     */
    private String deptTree;


    @Override
    public int compareTo(DeptInfoDTO o) {
        return o.deptId.compareTo(this.deptId);
    }
}
