package com.vediofun.auth.entity;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 登录响应实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String userType;
        private Boolean enabled;
        private List<String> roles;
        private List<String> permissions;
    }
} 