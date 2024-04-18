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

    /**
     * orWhere 默认为空,无效果;
     * 适用于未分配数据权限的数据行,如: user_id = 0  表示该行数据未分配给人,可以给所有人查看
     * orWhere = "user_id = 0"
     * 效果: 如果不为空,将会为您拼接 (user_id = 0 or user_id in {DataScope_sql});
     */
    private String orWhere = "";
}
