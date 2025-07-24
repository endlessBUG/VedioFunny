package com.vediofun.auth.repository;

import com.vediofun.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleCode(String roleCode);
    
    /**
     * 根据角色ID列表查询角色信息
     */
    @Query(value = "SELECT * FROM roles WHERE id IN (:roleIds) AND status = 1", 
           nativeQuery = true)
    List<Role> findRolesByIds(@Param("roleIds") List<Long> roleIds);
} 