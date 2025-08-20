package com.company.financial.service.impl;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.auth.*;
import com.company.financial.entity.*;
import com.company.financial.exception.DataNotFoundException;
import com.company.financial.exception.BusinessException;
import com.company.financial.repository.*;
import com.company.financial.service.RoleService;
import com.company.financial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
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
    private final RolePermissionRepository rolePermissionRepository;
    
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
    
    @Override
    @Transactional
    public RoleDTO createRole(RoleCreateDTO createDTO) {
        log.info("创建角色: code={}, name={}", createDTO.getCode(), createDTO.getName());
        
        // 检查角色编码是否已存在
        if (roleRepository.existsByCodeAndDeletedFalse(createDTO.getCode())) {
            throw new BusinessException("角色编码已存在");
        }
        
        Role role = new Role();
        role.setCode(createDTO.getCode());
        role.setName(createDTO.getName());
        role.setDescription(createDTO.getDescription());
        role.setStatus(createDTO.getStatus());
        role.setCreatedBy(SecurityUtils.getCurrentUserId());
        role.setCreatedAt(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        return convertToRoleDTO(savedRole);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public RoleDTO updateRole(String roleId, RoleUpdateDTO updateDTO) {
        log.info("更新角色: roleId={}", roleId);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        role.setName(updateDTO.getName());
        role.setDescription(updateDTO.getDescription());
        if (updateDTO.getStatus() != null) {
            role.setStatus(updateDTO.getStatus());
        }
        role.setUpdatedBy(SecurityUtils.getCurrentUserId());
        role.setUpdatedAt(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        return convertToRoleDTO(savedRole);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public void deleteRole(String roleId) {
        log.info("删除角色: roleId={}", roleId);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        // 检查是否有用户关联此角色
        if (userRoleRepository.existsByRoleId(roleId)) {
            throw new BusinessException("该角色下还有用户，无法删除");
        }
        
        role.setDeleted(true);
        role.setUpdatedBy(SecurityUtils.getCurrentUserId());
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
    }
    
    @Override
    public ResponsePageDataEntity<RoleDTO> queryRoles(RoleQueryDTO queryDTO) {
        log.debug("分页查询角色: {}", queryDTO);
        
        // 构建查询条件
        Specification<Role> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 基本条件
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            
            // 角色名称模糊查询
            if (StringUtils.hasText(queryDTO.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + queryDTO.getName() + "%"));
            }
            
            // 角色编码模糊查询
            if (StringUtils.hasText(queryDTO.getCode())) {
                predicates.add(criteriaBuilder.like(root.get("code"), "%" + queryDTO.getCode() + "%"));
            }
            
            // 状态查询
            if (queryDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 构建分页和排序
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(queryDTO.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                queryDTO.getSortBy()
        );
        Pageable pageable = PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize(), sort);
        
        // 执行查询
        Page<Role> rolePage = roleRepository.findAll(spec, pageable);
        
        // 转换结果
        List<RoleDTO> roleDTOs = rolePage.getContent().stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
        
        return new ResponsePageDataEntity<>(
                roleDTOs,
                rolePage.getTotalElements(),
                rolePage.getTotalPages()
        );
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public void updateRoleStatus(String roleId, Integer status) {
        log.info("更新角色状态: roleId={}, status={}", roleId, status);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        role.setStatus(status);
        role.setUpdatedBy(SecurityUtils.getCurrentUserId());
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
    }
    
    @Override
    public List<PermissionDTO> getRolePermissions(String roleId) {
        log.debug("获取角色权限: roleId={}", roleId);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        return role.getPermissions().stream()
                .filter(permission -> !permission.getDeleted())
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public void assignRolePermissions(String roleId, List<String> permissionIds) {
        log.info("分配角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        // 获取权限实体
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new DataNotFoundException("部分权限不存在");
        }
        
        // 添加新权限
        for (Permission permission : permissions) {
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId())) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permission.getId());
                rolePermission.setCreatedBy(SecurityUtils.getCurrentUserId());
                rolePermission.setCreatedAt(LocalDateTime.now());
                rolePermissionRepository.save(rolePermission);
            }
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public void removeRolePermissions(String roleId, List<String> permissionIds) {
        log.info("移除角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        // 删除指定权限
        rolePermissionRepository.deleteByRoleIdAndPermissionIdIn(roleId, permissionIds);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userPermissionCheck", "userRoleCheck"}, allEntries = true)
    public void saveRolePermissions(String roleId, List<String> permissionIds) {
        log.info("保存角色权限配置: roleId={}, permissionIds={}", roleId, permissionIds);
        
        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new DataNotFoundException("角色不存在"));
        
        // 删除原有所有权限
        rolePermissionRepository.deleteByRoleId(roleId);
        
        if (permissionIds != null && !permissionIds.isEmpty()) {
            // 验证权限是否存在
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                throw new DataNotFoundException("部分权限不存在");
            }
            
            // 添加新权限
            for (String permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreatedBy(SecurityUtils.getCurrentUserId());
                rolePermission.setCreatedAt(LocalDateTime.now());
                rolePermissionRepository.save(rolePermission);
            }
        }
    }
}