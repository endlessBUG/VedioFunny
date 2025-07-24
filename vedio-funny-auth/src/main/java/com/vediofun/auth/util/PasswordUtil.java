package com.vediofun.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成随机盐值的加密密码
     */
    public String encodePassword(String rawPassword) {
        return encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return matches(rawPassword, encodedPassword);
    }
} 