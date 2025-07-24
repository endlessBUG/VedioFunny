package com.vediofun.auth.config;

import com.vediofun.auth.entity.User;
import com.vediofun.auth.repository.UserRepository;
import com.vediofun.auth.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * 数据初始化配置
 * 在应用启动时检查并创建测试用户，确保密码使用BCrypt加密
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    /**
     * 应用启动时的数据初始化
     */
    @Bean
    public ApplicationRunner dataInitializer() {
        return args -> {
            log.info("开始初始化测试用户数据...");
            
            // 创建管理员用户
            createUserIfNotExists("admin", "123456", "管理员", 
                    "admin@vediofun.com", User.UserType.ADMIN,
                    "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            
            // 创建测试用户
            createUserIfNotExists("test", "123456", "测试用户", 
                    "test@vediofun.com", User.UserType.USER,
                    "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png");
            
            // 创建演示用户
            createUserIfNotExists("demo", "123456", "演示用户", 
                    "demo@vediofun.com", User.UserType.USER,
                    "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            
            log.info("测试用户数据初始化完成！");
        };
    }

    /**
     * 创建用户（如果不存在）
     */
    private void createUserIfNotExists(String username, String password, String nickname, 
                                       String email, User.UserType userType, String avatar) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordUtil.encodePassword(password)); // 使用BCrypt加密
            user.setNickname(nickname);
            user.setEmail(email);
            user.setUserType(userType);
            user.setAvatar(avatar);
            user.setStatus(1); // 启用状态
            user.setCreatedTime(LocalDateTime.now());
            user.setUpdatedTime(LocalDateTime.now());
            
            userRepository.save(user);
            log.info("创建测试用户: {} ({})", username, userType);
        } else {
            log.debug("用户已存在，跳过创建: {}", username);
        }
    }
} 