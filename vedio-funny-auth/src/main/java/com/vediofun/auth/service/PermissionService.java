package com.vediofun.auth.service;

import java.util.List;
import java.util.Map;

/**
 * 权限服务接口
 * 避免跨表JOIN查询，支持分布式数据库扩展
 */
public interface PermissionService {
    
    /**
     * 获取用户角色列表
     * 步骤：user_roles -> roles
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 获取用户权限列表
     * 步骤：user_roles -> role_permissions -> permissions
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 批量获取多个用户的角色权限信息
     * 用于减少数据库查询次数
     */
    Map<Long, List<String>> getBatchUserRoles(List<Long> userIds);
    
    /**
     * 批量获取多个用户的权限信息
     * 用于减少数据库查询次数
     */
    Map<Long, List<String>> getBatchUserPermissions(List<Long> userIds);
} 