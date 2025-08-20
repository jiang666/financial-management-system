package com.company.financial.controller;

import com.company.financial.common.ResponseEntity;
import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;
import com.company.financial.dto.auth.RoleCreateDTO;
import com.company.financial.dto.auth.RoleUpdateDTO;
import com.company.financial.dto.auth.RoleQueryDTO;
import com.company.financial.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
@Validated
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        log.info("创建角色请求: {}", createDTO.getName());
        RoleDTO role = roleService.createRole(createDTO);
        return ResponseEntity.success(role);
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<RoleDTO> updateRole(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId,
            @Valid @RequestBody RoleUpdateDTO updateDTO) {
        log.info("更新角色请求: {}", roleId);
        RoleDTO role = roleService.updateRole(roleId, updateDTO);
        return ResponseEntity.success(role);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @Operation(summary = "删除角色", description = "软删除角色")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId) {
        log.info("删除角色请求: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.success();
    }
    
    /**
     * 分页查询角色
     */
    @GetMapping
    @Operation(summary = "查询角色列表", description = "分页查询角色列表")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponsePageDataEntity<RoleDTO>> queryRoles(
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "角色编码") @RequestParam(required = false) String code,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        RoleQueryDTO queryDTO = new RoleQueryDTO();
        queryDTO.setName(name);
        queryDTO.setCode(code);
        queryDTO.setStatus(status);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setSortBy(sortBy);
        queryDTO.setSortDir(sortDir);
        
        ResponsePageDataEntity<RoleDTO> result = roleService.queryRoles(queryDTO);
        return ResponseEntity.success(result);
    }
    
    /**
     * 获取所有角色(简单列表)
     */
    @GetMapping("/list")
    @Operation(summary = "获取角色简单列表", description = "获取所有可用角色的简单列表")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.success(roles);
    }
    
    /**
     * 更新角色状态
     */
    @PatchMapping("/{roleId}/status")
    @Operation(summary = "更新角色状态", description = "启用或禁用角色")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> updateRoleStatus(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId,
            @Parameter(description = "状态：1-启用 0-禁用") @RequestParam @NotNull Integer status) {
        log.info("更新角色状态请求: roleId={}, status={}", roleId, status);
        roleService.updateRoleStatus(roleId, status);
        return ResponseEntity.success();
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
    
    /**
     * 获取角色的权限
     */
    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<PermissionDTO>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId) {
        List<PermissionDTO> permissions = roleService.getRolePermissions(roleId);
        return ResponseEntity.success(permissions);
    }
    
    /**
     * 分配角色权限
     */
    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "分配角色权限", description = "为角色分配权限")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> assignRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId,
            @Parameter(description = "权限ID列表") @RequestBody @NotNull List<String> permissionIds) {
        log.info("分配角色权限请求: roleId={}, permissionIds={}", roleId, permissionIds);
        roleService.assignRolePermissions(roleId, permissionIds);
        return ResponseEntity.success();
    }
    
    /**
     * 移除角色权限
     */
    @DeleteMapping("/{roleId}/permissions")
    @Operation(summary = "移除角色权限", description = "移除角色的权限")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> removeRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId,
            @Parameter(description = "权限ID列表") @RequestBody @NotNull List<String> permissionIds) {
        log.info("移除角色权限请求: roleId={}, permissionIds={}", roleId, permissionIds);
        roleService.removeRolePermissions(roleId, permissionIds);
        return ResponseEntity.success();
    }
    
    /**
     * 保存角色权限配置
     */
    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "保存角色权限配置", description = "保存角色的完整权限配置")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> saveRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotBlank String roleId,
            @Parameter(description = "权限ID列表") @RequestBody @NotNull List<String> permissionIds) {
        log.info("保存角色权限配置请求: roleId={}, permissionIds={}", roleId, permissionIds);
        roleService.saveRolePermissions(roleId, permissionIds);
        return ResponseEntity.success();
    }
}