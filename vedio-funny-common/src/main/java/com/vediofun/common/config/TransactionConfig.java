package com.vediofun.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * 事务管理配置类
 * 
 * 只有在JPA相关类存在时才生效
 * 这样Gateway等不需要数据库的服务就不会加载此配置
 * 
 * 解决自动提交冲突，确保正确的事务管理
 * 
 * @author VedioFun Team
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnClass({EntityManagerFactory.class, JpaTransactionManager.class})
public class TransactionConfig {

    /**
     * JPA事务管理器
     * 
     * @param entityManagerFactory 实体管理器工厂
     * @return 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // 设置事务超时时间（秒）
        transactionManager.setDefaultTimeout(30);
        
        // 设置在事务回滚时验证现有事务
        transactionManager.setValidateExistingTransaction(true);
        
        // 设置嵌套事务支持
        transactionManager.setNestedTransactionAllowed(true);
        
        return transactionManager;
    }
} 