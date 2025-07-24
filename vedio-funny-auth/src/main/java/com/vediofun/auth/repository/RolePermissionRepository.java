package com.vediofun.auth.repository;

import com.vediofun.auth.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 角色权限关系Repository
 * 专门处理role_permissions表的查询，避免JOIN操作
 */
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    /**
     * 根据角色ID列表查询权限ID列表
     */
    @Query(value = "SELECT DISTINCT permission_id FROM role_permissions WHERE role_id IN (:roleIds)", 
           nativeQuery = true)
    List<Long> findPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    /**
     * 删除角色的所有权限
     */
    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
    void deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 插入角色权限关系
     */
    @Modifying
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)", 
           nativeQuery = true)
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
} 