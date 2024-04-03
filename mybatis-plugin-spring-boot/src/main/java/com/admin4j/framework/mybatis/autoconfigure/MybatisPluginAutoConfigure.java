package com.admin4j.framework.mybatis.autoconfigure;

import com.admin4j.framework.mybatis.*;
import com.admin4j.framework.mybatis.interceptor.SqlInterceptor;
import com.admin4j.framework.mybatis.plugin.DataScopeInterceptor;
import com.admin4j.framework.mybatis.plugin.SqlExistInterceptor;
import com.admin4j.framework.mybatis.plugin.SqlInInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author andanyang
 * @since 2023/7/3 11:28
 */
// @ConditionalOnBean({SqlSessionFactory.class})
@AutoConfigureBefore(name = "com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration")
public class MybatisPluginAutoConfigure {

    @Bean
    @ConditionalOnBean(IAppendInSqlHandler.class)
    @ConditionalOnMissingBean(SqlInInterceptor.class)
    public SqlInInterceptor sqlInInterceptor(IAppendInSqlHandler appendInSqlHandler) {
        return new SqlInInterceptor(appendInSqlHandler);
    }

    @Bean
    @ConditionalOnBean(IAppendExistSqlHandler.class)
    @ConditionalOnMissingBean(SqlExistInterceptor.class)
    public SqlExistInterceptor sqlExistInterceptor(IAppendExistSqlHandler sqlExistService) {
        return new SqlExistInterceptor(sqlExistService);
    }

    @Bean
    @ConditionalOnBean(IDataScopeInfoHandler.class)
    @ConditionalOnMissingBean(IDataScopeTableExpression.class)
    public IDataScopeTableExpression dataScopeTableExpression() {
        return new IDataScopeTableExpression();
    }

    @Bean
    @ConditionalOnBean({IDataScopeInfoHandler.class, IDataScopeTableExpression.class})
    @ConditionalOnMissingBean(DataScopeInterceptor.class)
    public DataScopeInterceptor dataScopeInterceptor(IDataScopeInfoHandler dataScopeInfoHandler, IDataScopeTableExpression IDataScopeTableExpression) {
        return new DataScopeInterceptor(dataScopeInfoHandler, IDataScopeTableExpression);
    }

    @Bean
    @ConditionalOnBean(SqlInterceptor.class)
    public MybatisInterceptor mybatisInterceptor(List<SqlInterceptor> sqlInterceptors) {
        return new MybatisInterceptor(sqlInterceptors);
    }
}
