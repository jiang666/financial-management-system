package com.company.financial.controller;

import com.company.financial.common.response.ResponseEntity;
import com.company.financial.common.response.ResponsePageDataEntity;
import com.company.financial.dto.user.*;
import com.company.financial.service.UserService;
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
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
@Validated
public class UserController {
    
    private final UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserDetailDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        log.info("创建用户请求: {}", createDTO.getUsername());
        UserDetailDTO user = userService.createUser(createDTO);
        return ResponseEntity.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDetailDTO> updateUser(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("更新用户请求: {}", userId);
        UserDetailDTO user = userService.updateUser(userId, updateDTO);
        return ResponseEntity.success(user);
    }
    
    /**
     * 获取用户详细信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserDetailDTO> getUserById(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId) {
        UserDetailDTO user = userService.getUserById(userId);
        return ResponseEntity.success(user);
    }
    
    /**
     * 分页查询用户
     */
    @GetMapping
    @Operation(summary = "查询用户列表", description = "分页查询用户列表")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponsePageDataEntity<UserDetailDTO>> queryUsers(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "部门ID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色ID") @RequestParam(required = false) String roleId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setUsername(username);
        queryDTO.setEmail(email);
        queryDTO.setRealName(realName);
        queryDTO.setPhone(phone);
        queryDTO.setDepartmentId(departmentId);
        queryDTO.setStatus(status);
        queryDTO.setRoleId(roleId);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setSortBy(sortBy);
        queryDTO.setSortDir(sortDir);
        
        ResponsePageDataEntity<UserDetailDTO> result = userService.queryUsers(queryDTO);
        return ResponseEntity.success(result);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "软删除用户")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId) {
        log.info("删除用户请求: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.success();
    }
    
    /**
     * 启用/禁用用户
     */
    @PatchMapping("/{userId}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "状态：1-启用 0-禁用") @RequestParam @NotNull Integer status) {
        log.info("更新用户状态请求: userId={}, status={}", userId, status);
        userService.updateUserStatus(userId, status);
        return ResponseEntity.success();
    }
    
    /**
     * 重置用户密码
     */
    @PatchMapping("/{userId}/password")
    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> resetUserPassword(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "新密码") @RequestParam @NotBlank String newPassword) {
        log.info("重置用户密码请求: {}", userId);
        userService.resetUserPassword(userId, newPassword);
        return ResponseEntity.success();
    }
    
    /**
     * 分配用户角色
     */
    @PostMapping("/{userId}/roles")
    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> assignUserRoles(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "角色ID列表") @RequestBody @NotNull List<String> roleIds) {
        log.info("分配用户角色请求: userId={}, roleIds={}", userId, roleIds);
        userService.assignUserRoles(userId, roleIds);
        return ResponseEntity.success();
    }
    
    /**
     * 移除用户角色
     */
    @DeleteMapping("/{userId}/roles")
    @Operation(summary = "移除用户角色", description = "移除用户的角色")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> removeUserRoles(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "角色ID列表") @RequestBody @NotNull List<String> roleIds) {
        log.info("移除用户角色请求: userId={}, roleIds={}", userId, roleIds);
        userService.removeUserRoles(userId, roleIds);
        return ResponseEntity.success();
    }
}