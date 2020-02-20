package com.chuang.qapp.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author fandy.lin
 */
@Slf4j
@Configuration
public class DruidDataSourceConfig {
    @Bean(name = "pushOriginalDataSource")
    @Qualifier("pushOriginalDataSource")
    @ConfigurationProperties(prefix="datasource.push")
    public DataSource pushOriginalDataSource(){
        return DataSourceBuilder.create().build();
    }

//    @Bean(name = "commonOriginalDataSource")
//    @Qualifier("commonOriginalDataSource")
//    @ConfigurationProperties(prefix="datasource.common")
//    public DataSource commonOriginalDataSource(){
//        return DataSourceBuilder.create().build();
//    }
}
