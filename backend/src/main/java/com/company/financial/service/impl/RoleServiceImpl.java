package com.company.financial.service.impl;

import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;
import com.company.financial.entity.Permission;
import com.company.financial.entity.Role;
import com.company.financial.entity.UserRole;
import com.company.financial.exception.DataNotFoundException;
import com.company.financial.repository.PermissionRepository;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.repository.UserRoleRepository;
import com.company.financial.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findByDeletedFalseAndStatus(1);
        return roles.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleDTO getRoleById(String roleId) {
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        return convertToRoleDTO(role);
    }
    
    @Override
    public RoleDTO getRoleByCode(String roleCode) {
        Role role = roleRepository.findByCodeAndDeletedFalse(roleCode)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        return convertToRoleDTO(role);
    }
    
    @Override
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findByDeletedFalse();
        return permissions.stream()
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<PermissionDTO> getUserPermissions(String userId) {
        log.debug("获取用户权限: userId={}", userId);
        
        // 验证用户存在
        if (!userRepository.existsByIdAndDeletedFalse(userId)) {
            throw new DataNotFoundException("用户不存在");
        }
        
        // 获取用户角色
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取角色ID列表
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 获取所有权限（去重）
        Set<Permission> permissions = new HashSet<>();
        List<Role> roles = roleRepository.findAllById(roleIds);
        
        for (Role role : roles) {
            if (role.getStatus() == 1 && !role.getDeleted()) {
                permissions.addAll(role.getPermissions());
            }
        }
        
        return permissions.stream()
                .filter(permission -> !permission.getDeleted())
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoleDTO> getUserRoles(String userId) {
        log.debug("获取用户角色: userId={}", userId);
        
        // 验证用户存在
        if (!userRepository.existsByIdAndDeletedFalse(userId)) {
            throw new DataNotFoundException("用户不存在");
        }
        
        // 获取用户角色关联
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取角色ID列表
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 获取角色信息
        List<Role> roles = roleRepository.findAllById(roleIds);
        
        return roles.stream()
                .filter(role -> role.getStatus() == 1 && !role.getDeleted())
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "userPermissionCheck", key = "#userId + '_' + #permissionCode")
    public boolean hasPermission(String userId, String permissionCode) {
        log.debug("检查用户权限: userId={}, permissionCode={}", userId, permissionCode);
        
        // 获取用户所有权限
        List<PermissionDTO> permissions = getUserPermissions(userId);
        
        // 检查是否拥有指定权限
        return permissions.stream()
                .anyMatch(permission -> permissionCode.equals(permission.getCode()));
    }
    
    @Override
    @Cacheable(value = "userRoleCheck", key = "#userId + '_' + #roleCode")
    public boolean hasRole(String userId, String roleCode) {
        log.debug("检查用户角色: userId={}, roleCode={}", userId, roleCode);
        
        // 获取用户所有角色
        List<RoleDTO> roles = getUserRoles(userId);
        
        // 检查是否拥有指定角色
        return roles.stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }
    
    @Override
    public boolean hasAnyPermission(String userId, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        
        // 获取用户所有权限
        List<PermissionDTO> permissions = getUserPermissions(userId);
        Set<String> userPermissionCodes = permissions.stream()
                .map(PermissionDTO::getCode)
                .collect(Collectors.toSet());
        
        // 检查是否拥有任一权限
        return Arrays.stream(permissionCodes)
                .anyMatch(userPermissionCodes::contains);
    }
    
    @Override
    public boolean hasAllPermissions(String userId, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }
        
        // 获取用户所有权限
        List<PermissionDTO> permissions = getUserPermissions(userId);
        Set<String> userPermissionCodes = permissions.stream()
                .map(PermissionDTO::getCode)
                .collect(Collectors.toSet());
        
        // 检查是否拥有所有权限
        return Arrays.stream(permissionCodes)
                .allMatch(userPermissionCodes::contains);
    }
    
    private RoleDTO convertToRoleDTO(Role role) {
        // 获取角色权限
        List<PermissionDTO> permissionDTOs = role.getPermissions().stream()
                .filter(permission -> !permission.getDeleted())
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
        
        return RoleDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .status(role.getStatus())
                .createdAt(role.getCreatedAt())
                .permissions(permissionDTOs)
                .build();
    }
    
    private PermissionDTO convertToPermissionDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .build();
    }
}