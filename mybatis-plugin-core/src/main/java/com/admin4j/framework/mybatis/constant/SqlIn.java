package com.admin4j.framework.mybatis.constant;

import java.lang.annotation.*;

/**
 * @author andanyang
 * @since 2024/4/2 18:59
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqlIn {

    // 主表
    String mainTable();

    // 主表字段; in 前面的字段
    String mainField() default "id";

    // 子表
    String subTable();

    // 子表字段; in 前面的字段
    String subField() default "main_id";

    boolean ignore() default false;
}
