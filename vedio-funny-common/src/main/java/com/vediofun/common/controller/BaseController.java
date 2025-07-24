package com.vediofun.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vediofun.common.result.Result;
import com.vediofun.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 通用Controller基类
 * 
 * @author VedioFun Team
 */
@Tag(name = "基础CRUD接口", description = "通用的增删改查操作")
public abstract class BaseController {

    /**
     * 构建成功响应
     */
    protected <T> Result<T> success() {
        return Result.success();
    }

    /**
     * 构建成功响应（带数据）
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 构建成功响应（自定义消息）
     */
    protected <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    /**
     * 构建失败响应
     */
    protected <T> Result<T> error() {
        return Result.error();
    }

    /**
     * 构建失败响应（自定义消息）
     */
    protected <T> Result<T> error(String message) {
        return Result.error(message);
    }

    /**
     * 构建失败响应（自定义码和消息）
     */
    protected <T> Result<T> error(Integer code, String message) {
        return Result.error(code, message);
    }

    /**
     * 构建失败响应（使用结果码枚举）
     */
    protected <T> Result<T> error(ResultCode resultCode) {
        return Result.error(resultCode);
    }

    /**
     * 构建分页响应
     */
    protected <T> Result<Page<T>> pageSuccess(Page<T> page) {
        return Result.success("分页查询成功", page);
    }

    /**
     * 构建列表响应
     */
    protected <T> Result<List<T>> listSuccess(List<T> list) {
        return Result.success("查询成功", list);
    }

    /**
     * 验证ID参数
     */
    protected void validateId(Serializable id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
    }

    /**
     * 验证分页参数
     */
    protected void validatePageParams(Integer current, Integer size) {
        if (current == null || current < 1) {
            throw new IllegalArgumentException("当前页码必须大于0");
        }
        if (size == null || size < 1 || size > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }

    /**
     * 创建分页对象
     */
    protected <T> Page<T> createPage(Integer current, Integer size) {
        validatePageParams(current, size);
        return new Page<>(current, size);
    }

    /**
     * 健康检查接口
     */
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return success("服务运行正常");
    }

    /**
     * 服务信息接口
     */
    @Operation(summary = "服务信息", description = "获取服务基本信息")
    @GetMapping("/info")
    public Result<String> info() {
        return success("VedioFun微服务");
    }
} 