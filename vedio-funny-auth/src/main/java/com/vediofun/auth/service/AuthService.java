package com.vediofun.auth.service;

import com.vediofun.auth.entity.LoginRequest;
import com.vediofun.auth.entity.LoginResponse;
import com.vediofun.auth.dto.RegisterRequest;
import com.vediofun.auth.entity.User;
import com.vediofun.auth.repository.UserRepository;
import com.vediofun.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 验证令牌
     */
    Map<String, Object> validateToken(String token);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取用户信息
     */
    Map<String, Object> getUserInfo(String token);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册成功的用户信息
     */
    @Transactional
    User register(RegisterRequest request);

    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限代码列表
     */
    List<String> getUserPermissions(Long userId);
} 