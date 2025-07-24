package com.vediofun.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据自动填充处理器
 * 
 * 只有在MyBatis-Plus相关类存在时才生效
 * 这样Gateway等不需要数据库的服务就不会加载此配置
 * 
 * @author VedioFun Team
 */
@Component
@ConditionalOnClass({MetaObjectHandler.class, com.baomidou.mybatisplus.core.MybatisConfiguration.class})
public class MyBatisPlusConfiguration implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间自动填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 创建者ID (如果有用户上下文可以从中获取)
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUserId());
        
        // 逻辑删除标志
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间自动填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUserId());
    }

    /**
     * 获取当前用户ID
     * TODO: 从Security Context或ThreadLocal中获取当前用户信息
     */
    private String getCurrentUserId() {
        // 暂时返回系统用户，后续可以从Spring Security或自定义用户上下文中获取
        return "system";
    }
} 