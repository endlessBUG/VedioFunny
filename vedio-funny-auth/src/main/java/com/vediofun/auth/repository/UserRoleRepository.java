package com.vediofun.auth.repository;

import com.vediofun.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 用户角色关系Repository
 * 专门处理user_roles表的查询，避免JOIN操作
 */
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * 根据用户ID查询角色ID列表
     */
    @Query(value = "SELECT role_id FROM user_roles WHERE user_id = :userId", 
           nativeQuery = true)
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 插入用户角色关系
     */
    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)", 
           nativeQuery = true)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
} 