package com.vediofun.auth.service.impl;

import com.vediofun.auth.entity.LoginRequest;
import com.vediofun.auth.entity.LoginResponse;
import com.vediofun.auth.entity.RefreshToken;
import com.vediofun.auth.entity.User;
import com.vediofun.auth.entity.LoginLog;
import com.vediofun.auth.entity.Role;
import com.vediofun.auth.repository.UserRepository;
import com.vediofun.auth.repository.RoleRepository;
import com.vediofun.auth.repository.UserRoleRepository;
import com.vediofun.auth.repository.RolePermissionRepository;
import com.vediofun.auth.repository.PermissionRepository;
import com.vediofun.auth.service.AuthService;
import com.vediofun.auth.util.JwtUtil;
import com.vediofun.auth.util.PasswordUtil;
import com.vediofun.auth.dto.RegisterRequest;
import com.vediofun.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类 - 轻量级实现
 * 不依赖Spring Security，实现简单高效的用户认证和SSO功能
 * 
 * 权限查询架构：
 * - 禁用JOIN查询，各表独立查询，支持分布式数据库扩展
 * - 用户角色查询：user_roles -> roles (两步查询)
 * - 用户权限查询：user_roles -> role_permissions -> permissions (三步查询)
 * - 每个Repository只负责单表查询，避免跨表关联
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    // Redis键前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String USER_SESSION_PREFIX = "auth:session:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录尝试: {}", request.getUsername());

        try {
            // 查找用户
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

            // 验证密码
            if (!passwordUtil.verifyPassword(request.getPassword(), user.getPassword())) {
                log.warn("用户{}密码验证失败", request.getUsername());
                throw new RuntimeException("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() != 1) {
                log.warn("用户{}状态异常: {}", request.getUsername(), user.getStatus());
                throw new RuntimeException("用户已被禁用或锁定");
            }

            // 实现SSO：检查是否已有活跃会话，如果有则使旧令牌失效
            String existingSessionKey = USER_SESSION_PREFIX + user.getId();
            Set<Object> existingTokens = redisTemplate.opsForSet().members(existingSessionKey);
            if (existingTokens != null && !existingTokens.isEmpty()) {
                log.info("检测到用户{}已有活跃会话，执行SSO令牌更新", user.getUsername());
                for (Object token : existingTokens) {
                    // 将旧令牌加入黑名单
                    addToBlacklist((String) token);
                }
                // 清除旧会话
                redisTemplate.delete(existingSessionKey);
            }

            // 生成新的JWT令牌
            String accessToken = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getUserType().name()
            );
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            // SSO会话管理：记录新的会话令牌
            redisTemplate.opsForSet().add(existingSessionKey, accessToken);
            redisTemplate.expire(existingSessionKey, expiration, TimeUnit.MILLISECONDS);

            // 存储刷新令牌
            String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Map<String, Object> refreshData = new HashMap<>();
            refreshData.put("userId", user.getId());
            refreshData.put("username", user.getUsername());
            refreshData.put("accessToken", accessToken);
            redisTemplate.opsForValue().set(refreshKey, refreshData, refreshExpiration, TimeUnit.MILLISECONDS);

            // 更新用户登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(getClientIp()); // 需要从请求中获取IP
            userRepository.save(user);

            // 记录登录日志
            saveLoginLog(user, LoginLog.LoginType.PASSWORD, true, "登录成功");

            // 获取用户角色和权限
            List<String> userRoles = getUserRoles(user.getId());
            List<String> userPermissions = getUserPermissions(user.getId());

            // 打印用户权限信息
            log.info("=== 用户权限信息 ===");
            log.info("用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
            log.info("用户类型: {}", user.getUserType().name());
            log.info("角色列表: {}", userRoles);
            log.info("权限列表: {}", userPermissions);
            log.info("==================");

            // 构建用户信息
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .email(user.getEmail())
                    .userType(user.getUserType().name())
                    .enabled(user.getStatus() == 1)
                    .roles(userRoles)
                    .permissions(userPermissions)
                    .build();

            log.info("用户登录成功: {}, SSO会话已建立", user.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiration / 1000)
                    .userInfo(userInfo)
                    .build();

        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", request.getUsername(), e.getMessage());
            // 直接抛出原始错误信息，不包装成"运行时错误"
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        log.info("用户登出请求");
        
        try {
            // 解析令牌
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (username == null) {
                log.warn("登出失败：无效的令牌");
                return;
            }

            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                // 清除用户会话
                String sessionKey = USER_SESSION_PREFIX + user.getId();
                redisTemplate.delete(sessionKey);
                log.info("用户{}会话已清除", username);
            }

            // 将令牌加入黑名单
            addToBlacklist(token.replace("Bearer ", ""));

            log.info("用户{}登出成功", username);
            
        } catch (Exception e) {
            log.error("登出过程异常: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String actualToken = token.replace("Bearer ", "");
            
            // 检查令牌是否在黑名单中
            if (isTokenBlacklisted(actualToken)) {
                result.put("valid", false);
                result.put("error", "令牌已失效");
                return result;
            }
            
            // 验证令牌
            if (jwtUtil.isValidToken(actualToken)) {
                String username = jwtUtil.getUsernameFromToken(actualToken);
                Long userId = jwtUtil.getUserIdFromToken(actualToken);
                String userType = jwtUtil.getUserTypeFromToken(actualToken);
                
                result.put("valid", true);
                result.put("username", username);
                result.put("userId", userId);
                result.put("userType", userType);
                
                log.debug("令牌验证成功: 用户={}, ID={}", username, userId);
            } else {
                result.put("valid", false);
                result.put("error", "令牌无效或已过期");
                log.warn("令牌验证失败: 令牌无效或已过期");
            }
            
        } catch (Exception e) {
            log.error("令牌验证异常: {}", e.getMessage());
            result.put("valid", false);
            result.put("error", "令牌验证异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("刷新令牌请求");
        
        try {
            String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> refreshData = (Map<String, Object>) redisTemplate.opsForValue().get(refreshKey);
            
            if (refreshData == null) {
                throw new RuntimeException("刷新令牌无效或已过期");
            }
            
            String username = (String) refreshData.get("username");
            Long userId = Long.valueOf(refreshData.get("userId").toString());
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getUserType().name()
            );
            
            // 更新Redis中的令牌信息
            refreshData.put("accessToken", newAccessToken);
            redisTemplate.opsForValue().set(refreshKey, refreshData, refreshExpiration, TimeUnit.MILLISECONDS);
            
            // 更新用户会话
            String sessionKey = USER_SESSION_PREFIX + user.getId();
            redisTemplate.delete(sessionKey);
            redisTemplate.opsForSet().add(sessionKey, newAccessToken);
            redisTemplate.expire(sessionKey, expiration, TimeUnit.MILLISECONDS);
            
            // 构建用户信息
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .email(user.getEmail())
                    .userType(user.getUserType().name())
                    .enabled(true)
                    .build();
            
            log.info("令牌刷新成功: 用户={}", username);
            
            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // 刷新令牌保持不变
                    .tokenType("Bearer")
                    .expiresIn(expiration / 1000)
                    .userInfo(userInfo)
                    .build();
            
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            
            if (!jwtUtil.isValidToken(actualToken)) {
                throw new RuntimeException("令牌无效或已过期");
            }
            
            String username = jwtUtil.getUsernameFromToken(actualToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取用户角色和权限
            List<String> userRoles = getUserRoles(user.getId());
            List<String> userPermissions = getUserPermissions(user.getId());
            
            // 打印用户权限信息
            log.info("=== getUserInfo 用户权限信息 ===");
            log.info("用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
            log.info("用户类型: {}", user.getUserType().name());
            log.info("角色列表: {}", userRoles);
            log.info("权限列表: {}", userPermissions);
            log.info("================================");
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("userType", user.getUserType().name());
            userInfo.put("enabled", user.getStatus() == 1);
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            userInfo.put("roles", userRoles);
            userInfo.put("permissions", userPermissions);
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        try {
            // 无JOIN查询架构 - 第一步：查询用户的角色ID列表
            // 只查询user_roles表，避免跨表JOIN，支持分布式数据库
            List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
            
            if (roleIds.isEmpty()) {
                log.warn("用户{}没有分配任何角色", userId);
                return Collections.singletonList("ROLE_GUEST");
            }
            
            // 无JOIN查询架构 - 第二步：根据角色ID查询角色信息
            // 只查询roles表，各表独立查询
            List<Role> roles = roleRepository.findRolesByIds(roleIds);
            
            return roles.stream()
                    .filter(role -> role.getStatus() == 1) // 只返回启用的角色
                    .map(Role::getRoleCode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
            // 返回默认角色为访客
            return Collections.singletonList("ROLE_GUEST");
        }
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            // 无JOIN查询架构 - 第一步：查询用户的角色ID列表
            // 只查询user_roles表，避免跨表JOIN，支持分布式数据库
            List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
            
            if (roleIds.isEmpty()) {
                log.warn("用户{}没有分配任何角色", userId);
                return Arrays.asList("dashboard:view", "model:menu", "model:list", "model:market");
            }
            
            // 无JOIN查询架构 - 第二步：根据角色ID查询权限ID列表
            // 只查询role_permissions表，各表独立查询
            List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
            
            if (permissionIds.isEmpty()) {
                log.warn("用户{}的角色没有分配任何权限", userId);
                return Arrays.asList("dashboard:view", "model:menu", "model:list", "model:market");
            }
            
            // 无JOIN查询架构 - 第三步：根据权限ID查询权限代码
            // 只查询permissions表，各表独立查询
            List<String> permissions = permissionRepository.findPermissionCodesByIds(permissionIds);
            
            return permissions.stream()
                    .distinct() // 去重
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", e.getMessage());
            // 返回访客默认权限
            return Arrays.asList("dashboard:view", "model:menu", "model:list", "model:market");
        }
    }

    /**
     * 将令牌添加到黑名单
     */
    private void addToBlacklist(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", expiration, TimeUnit.MILLISECONDS);
            log.debug("令牌已加入黑名单: {}", token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            log.error("加入黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 检查令牌是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            log.error("检查黑名单失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        // 简化实现，实际应该从HttpServletRequest中获取
        return "127.0.0.1";
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(User user, LoginLog.LoginType loginType, boolean success, String message) {
        try {
            // 简化实现，可以根据需要记录详细的登录日志
            log.info("登录日志 - 用户: {}, 类型: {}, 结果: {}, 消息: {}", 
                    user.getUsername(), loginType, success ? "成功" : "失败", message);
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 检查手机号是否已存在
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }

        // 获取默认用户角色
        Role userRole = roleRepository.findByRoleCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException("默认角色不存在"));

        // 创建新用户（不设置角色关联）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordUtil.encodePassword(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setAvatar(request.getAvatar());
        user.setStatus(1); // 1: 正常状态

        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 直接在user_roles表中插入角色关系
        userRoleRepository.insertUserRole(savedUser.getId(), userRole.getId());
        
        log.info("用户注册成功: {}, 默认角色: {}", savedUser.getUsername(), userRole.getRoleCode());
        
        return savedUser;
    }
} 