package com.vediofun.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 德鲁伊数据源配置
 * 确保德鲁伊连接池正确配置和初始化
 *
 * @author VedioFun Team
 */
@Slf4j
@Configuration
@ConditionalOnClass(DruidDataSource.class)
public class DruidConfig {

    /**
     * 配置德鲁伊数据源
     * 使用@Primary确保优先使用德鲁伊而不是HikariCP
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() {
        log.info("🔧 Initializing Druid DataSource...");
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        log.info("✅ Druid DataSource initialized successfully");
        return dataSource;
    }

    /**
     * 配置完成后的回调
     */
    @Bean
    public DruidConfigurationCallback druidConfigurationCallback() {
        return new DruidConfigurationCallback();
    }

    /**
     * 德鲁伊配置回调类
     */
    public static class DruidConfigurationCallback {
        public DruidConfigurationCallback() {
            log.info("🎯 Druid configuration loaded successfully");
            log.info("📊 Druid monitoring will be available at: /druid/");
            log.info("👤 Druid monitor login: admin/123456");
        }
    }
} 