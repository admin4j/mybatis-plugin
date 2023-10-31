package com.admin4j.framework.mybatis.entity;

import lombok.Data;

/**
 * 数据表信息
 *
 * @author andanyang
 * @since 2023/10/31 10:59
 */
@Data
public class DataTableInfoDTO {
    /**
     * 资源模块，该资源的权限分配类型
     */
    private String module;

    /**
     * 资源表.表名
     */
    private String table;

    /**
     * 资源字段 关联ID字段 默认 user_id
     */
    private String field = "user_id";
}
