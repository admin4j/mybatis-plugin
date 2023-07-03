package com.admin4j.framework.mybatis.autoconfigure;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.plugin.DataScopeInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author andanyang
 * @since 2023/7/3 11:28
 */
public class MybatisPluginAutoConfigure {

    @Bean
    @ConditionalOnBean(IDataScopeInfoHandler.class)
    @ConditionalOnMissingBean(DataScopeInterceptor.class)
    public DataScopeInterceptor dataScopeInterceptor(IDataScopeInfoHandler dataScopeInfoHandler) {
        return new DataScopeInterceptor(dataScopeInfoHandler);
    }
}
