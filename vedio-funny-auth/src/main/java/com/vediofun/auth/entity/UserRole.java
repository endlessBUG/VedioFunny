package com.vediofun.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

/**
 * 用户角色关系实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles")
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
} 