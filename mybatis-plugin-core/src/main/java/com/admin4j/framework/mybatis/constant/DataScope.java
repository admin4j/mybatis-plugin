package com.admin4j.framework.mybatis.constant;

import java.lang.annotation.*;

/**
 * @author andanyang
 * @since 2023/6/28 16:54
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 资源模块，该资源的权限分配类型
     *
     * @return
     */
    String module() default "";

    /**
     * @return 资源表
     */
    String table() default "";

    /**
     * @return 资源字段 关联ID
     */
    String field() default "user_id";

    /**
     * orWhere 默认为空,无效果;
     * 适用于未分配数据权限的数据行,如: user_id = 0  表示该行数据未分配给人,可以给所有人查看
     * orWhere = "user_id = 0"
     * 效果: 如果不为空,将会为您拼接 (user_id = 0 or user_id in {DataScope_sql});
     *
     * @return
     */
    String orWhere() default "";

    boolean ignore() default false;
}
