package com.admin4j.framework.mybatis.exception;

/**
 * @author andanyang
 * @since 2023/7/3 8:59
 */
public class NoDataException extends MybatisPluginException {

    public NoDataException(String message) {
        super(message);
    }
}
