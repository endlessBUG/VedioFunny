package com.vediofun.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Favicon通用处理器
 * 处理浏览器自动请求的favicon.ico，避免404错误日志
 * 所有微服务都会自动继承此控制器
 */
@RestController
public class FaviconController {

    /**
     * 处理favicon.ico请求
     * 返回204 No Content状态码，告诉浏览器没有favicon
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
} 