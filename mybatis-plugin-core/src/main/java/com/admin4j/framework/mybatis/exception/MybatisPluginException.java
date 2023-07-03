package com.admin4j.framework.mybatis.exception;

/**
 * @author andanyang
 * @since 2023/6/29 10:40
 */
public class MybatisPluginException extends RuntimeException {

    public MybatisPluginException(String message) {
        super(message);
    }

    public MybatisPluginException(Throwable e, String message) {
        super(message, e);
    }
}
