package com.vediofun.auth.controller;

import com.vediofun.auth.dto.RegisterRequest;
import com.vediofun.auth.entity.LoginRequest;
import com.vediofun.auth.entity.LoginResponse;
import com.vediofun.auth.entity.User;
import com.vediofun.auth.repository.UserRepository;
import com.vediofun.auth.service.AuthService;
import com.vediofun.auth.util.JwtUtil;
import com.vediofun.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出、令牌验证等认证相关操作
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登录成功",
                "data", response
        ));
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，使令牌失效")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登出成功"
        ));
    }

    /**
     * 验证令牌
     */
    @Operation(summary = "验证令牌", description = "验证JWT令牌的有效性")
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = authService.validateToken(token);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "令牌验证成功",
                "data", result
        ));
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "令牌刷新成功",
                "data", response
        ));
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/userinfo")
    public Result<?> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            
            if (!jwtUtil.isValidToken(actualToken)) {
                return Result.error("令牌无效或已过期");
            }
            
            String username = jwtUtil.getUsernameFromToken(actualToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取用户角色和权限
            List<String> userRoles = authService.getUserRoles(user.getId());
            List<String> userPermissions = authService.getUserPermissions(user.getId());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("userType", user.getUserType().name());
            userInfo.put("enabled", user.getStatus() == 1);
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            userInfo.put("roles", userRoles);
            userInfo.put("permissions", userPermissions);
            
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查用户名是否存在
     */
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam(name = "username") String username) {
        boolean exists = authService.checkUsernameExists(username);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", exists ? "用户名已存在" : "用户名可用",
                "data", Map.of("exists", exists)
        ));
    }

    /**
     * 服务健康检查
     */
    @Operation(summary = "服务状态", description = "获取认证服务状态信息")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "vedio-funny-auth",
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0"
        ));
    }

    /**
     * 测试端点 - 用于前端测试连接
     */
    @Operation(summary = "测试连接", description = "测试前后端连接是否正常")
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "认证服务连接正常",
                "service", "vedio-funny-auth",
                "timestamp", System.currentTimeMillis(),
                "testAccounts", Map.of(
                        "admin", "123456",
                        "test", "123456"
                )
        ));
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户注册，返回用户基本信息")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return Result.success(user);
    }
} 