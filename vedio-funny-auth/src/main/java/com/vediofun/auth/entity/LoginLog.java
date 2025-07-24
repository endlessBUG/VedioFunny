package com.vediofun.auth.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "login_logs")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType = LoginType.PASSWORD;

    @Column(name = "login_status", nullable = false)
    private Integer loginStatus;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(length = 100)
    private String location;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 登录类型枚举
     */
    public enum LoginType {
        PASSWORD("密码登录"),
        SMS("短信登录"),
        OAUTH("第三方登录");

        private final String description;

        LoginType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查登录是否成功
     */
    public boolean isSuccess() {
        return loginStatus == 1;
    }
} 