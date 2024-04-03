package com.admin4j.framework.mybatis.constant;

import java.lang.annotation.*;

/**
 * @author andanyang
 * @since 2024/4/2 18:59
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqlExist {

    boolean ignore() default false;
}
