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
    String table();

    /**
     * @return 资源字段 关联ID
     */
    String field() default "user_id";
}
