package com.vediofun.auth.repository;

import com.vediofun.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限数据访问层
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据权限代码查找权限
     */
    Optional<Permission> findByPermissionCode(String permissionCode);
    
    /**
     * 检查权限代码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 根据权限ID列表查询权限代码列表
     */
    @Query(value = "SELECT permission_code FROM permissions WHERE id IN (:permissionIds) AND status = 1", 
           nativeQuery = true)
    List<String> findPermissionCodesByIds(@Param("permissionIds") List<Long> permissionIds);
} 