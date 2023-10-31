package com.admin4j.framework.mybatis.autoconfigure;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.IDataScopeTableExpression;
import com.admin4j.framework.mybatis.MybatisInterceptor;
import com.admin4j.framework.mybatis.interceptor.SqlInterceptor;
import com.admin4j.framework.mybatis.plugin.DataScopeInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author andanyang
 * @since 2023/7/3 11:28
 */
public class MybatisPluginAutoConfigure {

    @Bean
    @ConditionalOnBean(IDataScopeInfoHandler.class)
    @ConditionalOnMissingBean(DataScopeInterceptor.class)
    public DataScopeInterceptor dataScopeInterceptor(IDataScopeInfoHandler dataScopeInfoHandler, IDataScopeTableExpression IDataScopeTableExpression) {
        return new DataScopeInterceptor(dataScopeInfoHandler, IDataScopeTableExpression);
    }

    @Bean
    @ConditionalOnBean(IDataScopeInfoHandler.class)
    @ConditionalOnMissingBean(IDataScopeTableExpression.class)
    public IDataScopeTableExpression dataScopeTableExpression() {
        return new IDataScopeTableExpression();
    }

    @Bean
    @ConditionalOnBean(SqlInterceptor.class)
    public MybatisInterceptor mybatisInterceptor(List<SqlInterceptor> sqlInterceptors) {
        return new MybatisInterceptor(sqlInterceptors);
    }
}
