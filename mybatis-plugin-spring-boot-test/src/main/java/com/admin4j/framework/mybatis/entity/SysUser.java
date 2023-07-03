package com.admin4j.framework.mybatis.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author andanyang
 * @since 2023-05-29
 */
@Getter
@Setter
@Data
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */

    private Long userId;

    /**
     * 主部门ID
     */

    private Long deptId;
    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */

    private String email;

    /**
     * 手机号码
     */

    private String mobile;

    /**
     * 用户性别（1男 2女 0未知）
     */
    private Integer sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 密码
     */

    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */

    private Boolean delFlag;


    private Long createBy;


    private LocalDateTime createTime;


    private Long updateBy;


    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
    private String jwtSalt;
    private Long tenantId;
    private Long directLeader;
}
