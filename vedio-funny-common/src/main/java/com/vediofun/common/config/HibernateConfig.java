package com.vediofun.common.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Hibernate 通用配置类
 * 
 * 优化Hibernate性能，解决常见问题如MultipleBagFetchException
 * 
 * @author VedioFun Team
 */
@Configuration
public class HibernateConfig {

    /**
     * Hibernate属性自定义器
     * 配置避免MultipleBagFetchException和性能优化
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return new HibernatePropertiesCustomizer() {
            @Override
            public void customize(Map<String, Object> hibernateProperties) {
                // 启用批量处理
                hibernateProperties.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, 16);
                
                // 优化查询性能
                hibernateProperties.put(AvailableSettings.ORDER_INSERTS, true);
                hibernateProperties.put(AvailableSettings.ORDER_UPDATES, true);
                hibernateProperties.put(AvailableSettings.BATCH_VERSIONED_DATA, true);
                
                // 启用查询缓存 (可选)
                hibernateProperties.put(AvailableSettings.USE_QUERY_CACHE, false);
                hibernateProperties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
                
                // 启用统计信息 (开发环境)
                hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, false);
                
                // 优化连接处理 - 移除自动提交配置冲突
                // hibernateProperties.put(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, true);
                
                // 避免MultipleBagFetchException的额外配置
                // 当使用Set替代List时，这些配置提供额外保障
                hibernateProperties.put(AvailableSettings.DEFAULT_LIST_SEMANTICS, "BAG");
                
                // 启用延迟加载优化
                hibernateProperties.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, false);
                
                // 配置命名策略
                hibernateProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, 
                    "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
                hibernateProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                    "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl");
            }
        };
    }
} 