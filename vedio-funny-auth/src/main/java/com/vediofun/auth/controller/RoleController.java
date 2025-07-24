package com.vediofun.auth.controller;

import com.vediofun.auth.entity.Role;
import com.vediofun.auth.entity.Permission;
import com.vediofun.auth.repository.RoleRepository;
import com.vediofun.auth.repository.PermissionRepository;
import com.vediofun.auth.repository.RolePermissionRepository;
import com.vediofun.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> listRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            log.info("获取角色列表成功，共 {} 个角色", roles.size());
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加角色
     */
    @PostMapping("/add")
    public Result<Role> addRole(@RequestBody Role role) {
        try {
            // 检查角色代码是否已存在
            if (roleRepository.findByRoleCode(role.getRoleCode()).isPresent()) {
                return Result.error("角色代码已存在");
            }
            
            Role savedRole = roleRepository.save(role);
            log.info("添加角色成功: {}", savedRole.getRoleName());
            return Result.success(savedRole);
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return Result.error("添加角色失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PostMapping("/update")
    public Result<Role> updateRole(@RequestBody Role role) {
        try {
            if (role.getId() == null) {
                return Result.error("角色ID不能为空");
            }
            
            if (!roleRepository.existsById(role.getId())) {
                return Result.error("角色不存在");
            }
            
            Role savedRole = roleRepository.save(role);
            log.info("更新角色成功: {}", savedRole.getRoleName());
            return Result.success(savedRole);
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error("更新角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/permissions")
    public Result<List<Long>> getRolePermissions(@RequestParam(name = "roleId") Long roleId) {
        try {
            List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(List.of(roleId));
            log.info("获取角色权限成功，角色ID: {}, 权限数量: {}", roleId, permissionIds.size());
            return Result.success(permissionIds);
        } catch (Exception e) {
            log.error("获取角色权限失败", e);
            return Result.error("获取角色权限失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色权限
     */
    @PostMapping("/permissions")
    @Transactional
    public Result<String> updateRolePermissions(@RequestParam(name = "roleId") Long roleId, @RequestBody List<Long> permissionIds) {
        try {
            // 检查角色是否存在
            if (!roleRepository.existsById(roleId)) {
                return Result.error("角色不存在");
            }

            // 删除角色的所有权限
            rolePermissionRepository.deleteByRoleId(roleId);

            // 添加新的权限
            if (permissionIds != null && !permissionIds.isEmpty()) {
                for (Long permissionId : permissionIds) {
                    rolePermissionRepository.insertRolePermission(roleId, permissionId);
                }
            }

            log.info("更新角色权限成功，角色ID: {}, 权限数量: {}", roleId, permissionIds.size());
            return Result.success("权限更新成功");
        } catch (Exception e) {
            log.error("更新角色权限失败", e);
            return Result.error("更新角色权限失败: " + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRole(@PathVariable("id") Long id) {
        try {
            if (!roleRepository.existsById(id)) {
                return Result.error("角色不存在");
            }
            
            roleRepository.deleteById(id);
            log.info("删除角色成功，ID: {}", id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error("删除角色失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable("id") Long id) {
        try {
            return roleRepository.findById(id)
                    .map(Result::success)
                    .orElse(Result.error("角色不存在"));
        } catch (Exception e) {
            log.error("获取角色失败", e);
            return Result.error("获取角色失败: " + e.getMessage());
        }
    }
} 