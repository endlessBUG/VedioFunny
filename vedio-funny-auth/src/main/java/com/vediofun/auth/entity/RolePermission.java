package com.vediofun.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

/**
 * 角色权限关系实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role_permissions")
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
} 