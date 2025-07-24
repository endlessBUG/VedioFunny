package com.vediofun.auth.controller;

import com.vediofun.auth.entity.Permission;
import com.vediofun.auth.repository.PermissionRepository;
import com.vediofun.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionRepository permissionRepository;

    /**
     * 获取所有权限列表（仅菜单权限）
     */
    @GetMapping("/list")
    public Result<List<Permission>> listPermissions() {
        try {
            List<Permission> allPermissions = permissionRepository.findAll();
            // 只返回菜单权限
            List<Permission> menuPermissions = allPermissions.stream()
                    .filter(permission -> "MENU".equals(permission.getResourceType().name()))
                    .collect(Collectors.toList());
            
            log.info("获取菜单权限列表成功，共 {} 个权限", menuPermissions.size());
            return Result.success(menuPermissions);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加权限
     */
    @PostMapping("/add")
    public Result<Permission> addPermission(@RequestBody Permission permission) {
        try {
            // 检查权限代码是否已存在
            if (permissionRepository.findByPermissionCode(permission.getPermissionCode()).isPresent()) {
                return Result.error("权限代码已存在");
            }
            
            Permission savedPermission = permissionRepository.save(permission);
            log.info("添加权限成功: {}", savedPermission.getPermissionName());
            return Result.success(savedPermission);
        } catch (Exception e) {
            log.error("添加权限失败", e);
            return Result.error("添加权限失败: " + e.getMessage());
        }
    }

    /**
     * 更新权限
     */
    @PostMapping("/update")
    public Result<Permission> updatePermission(@RequestBody Permission permission) {
        try {
            if (permission.getId() == null) {
                return Result.error("权限ID不能为空");
            }
            
            if (!permissionRepository.existsById(permission.getId())) {
                return Result.error("权限不存在");
            }
            
            Permission savedPermission = permissionRepository.save(permission);
            log.info("更新权限成功: {}", savedPermission.getPermissionName());
            return Result.success(savedPermission);
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return Result.error("更新权限失败: " + e.getMessage());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePermission(@PathVariable("id") Long id) {
        try {
            if (!permissionRepository.existsById(id)) {
                return Result.error("权限不存在");
            }
            
            permissionRepository.deleteById(id);
            log.info("删除权限成功，ID: {}", id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return Result.error("删除权限失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    public Result<Permission> getPermissionById(@PathVariable("id") Long id) {
        try {
            return permissionRepository.findById(id)
                    .map(Result::success)
                    .orElse(Result.error("权限不存在"));
        } catch (Exception e) {
            log.error("获取权限失败", e);
            return Result.error("获取权限失败: " + e.getMessage());
        }
    }
} 