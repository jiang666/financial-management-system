package com.company.financial.controller;

import com.company.financial.common.response.ResponseEntity;
import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;
import com.company.financial.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 获取所有角色
     */
    @GetMapping
    @Operation(summary = "获取角色列表", description = "获取所有可用角色")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.success(roles);
    }
    
    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<RoleDTO> getRoleById(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId) {
        RoleDTO role = roleService.getRoleById(roleId);
        return ResponseEntity.success(role);
    }
    
    /**
     * 获取所有权限
     */
    @GetMapping("/permissions")
    @Operation(summary = "获取权限列表", description = "获取所有系统权限")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = roleService.getAllPermissions();
        return ResponseEntity.success(permissions);
    }
    
    /**
     * 获取用户的所有权限
     */
    @GetMapping("/user/{userId}/permissions")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<PermissionDTO>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId) {
        List<PermissionDTO> permissions = roleService.getUserPermissions(userId);
        return ResponseEntity.success(permissions);
    }
    
    /**
     * 检查用户是否拥有指定权限
     */
    @GetMapping("/user/{userId}/permissions/{permissionCode}/check")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<Boolean> checkUserPermission(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "权限编码") @PathVariable @NotBlank String permissionCode) {
        boolean hasPermission = roleService.hasPermission(userId, permissionCode);
        return ResponseEntity.success(hasPermission);
    }
}