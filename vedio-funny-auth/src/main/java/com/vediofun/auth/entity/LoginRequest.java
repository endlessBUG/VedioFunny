package com.vediofun.auth.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求实体
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 记住我
     */
    private Boolean remember = false;
    
    /**
     * 验证码
     */
    private String captcha;
    
    /**
     * 验证码键
     */
    private String captchaKey;
} 