package com.company.financial.service.impl;

import com.company.financial.common.response.ResponsePageDataEntity;
import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;
import com.company.financial.dto.user.*;
import com.company.financial.entity.Permission;
import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.entity.UserRole;
import com.company.financial.exception.BusinessException;
import com.company.financial.exception.DataNotFoundException;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.repository.UserRoleRepository;
import com.company.financial.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserDetailDTO createUser(UserCreateDTO createDTO) {
        log.info("创建用户: {}", createDTO.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.findByUsernameAndDeletedFalse(createDTO.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(createDTO.getEmail()) && 
            userRepository.findByEmailAndDeletedFalse(createDTO.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 创建用户实体
        User user = User.builder()
                .username(createDTO.getUsername())
                .password(passwordEncoder.encode(createDTO.getPassword()))
                .email(createDTO.getEmail())
                .realName(createDTO.getRealName())
                .phone(createDTO.getPhone())
                .departmentId(createDTO.getDepartmentId())
                .status(createDTO.getStatus())
                .createdBy(getCurrentUserId())
                .build();
        
        // 保存用户
        user = userRepository.save(user);
        
        // 分配角色
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            assignUserRoles(user.getId(), createDTO.getRoleIds());
        }
        
        return convertToDetailDTO(user);
    }
    
    @Override
    @Transactional
    public UserDetailDTO updateUser(String userId, UserUpdateDTO updateDTO) {
        log.info("更新用户: {}", userId);
        
        User user = getUserEntityById(userId);
        
        // 乐观锁检查
        if (updateDTO.getVersion() != null && !user.getVersion().equals(updateDTO.getVersion())) {
            throw new BusinessException("数据已被其他用户修改，请刷新后重试");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getEmail()) && 
            !updateDTO.getEmail().equals(user.getEmail()) &&
            userRepository.findByEmailAndDeletedFalse(updateDTO.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已被其他用户使用");
        }
        
        // 更新用户信息
        if (StringUtils.hasText(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            user.setPhone(updateDTO.getPhone());
        }
        if (StringUtils.hasText(updateDTO.getDepartmentId())) {
            user.setDepartmentId(updateDTO.getDepartmentId());
        }
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }
        
        user.setUpdatedBy(getCurrentUserId());
        user = userRepository.save(user);
        
        // 更新角色分配
        if (updateDTO.getRoleIds() != null) {
            // 先移除所有现有角色
            userRoleRepository.deleteByUserId(userId);
            // 重新分配角色
            if (!updateDTO.getRoleIds().isEmpty()) {
                assignUserRoles(userId, updateDTO.getRoleIds());
            }
        }
        
        return convertToDetailDTO(user);
    }
    
    @Override
    public UserDetailDTO getUserById(String userId) {
        User user = getUserEntityById(userId);
        return convertToDetailDTO(user);
    }
    
    @Override
    public UserDetailDTO getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new DataNotFoundException("用户不存在"));
        return convertToDetailDTO(user);
    }
    
    @Override
    public ResponsePageDataEntity<UserDetailDTO> queryUsers(UserQueryDTO queryDTO) {
        log.info("查询用户列表: {}", queryDTO);
        
        // 构建查询条件
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 未删除的用户
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            
            // 用户名模糊查询
            if (StringUtils.hasText(queryDTO.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + queryDTO.getUsername() + "%"));
            }
            
            // 邮箱模糊查询
            if (StringUtils.hasText(queryDTO.getEmail())) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + queryDTO.getEmail() + "%"));
            }
            
            // 真实姓名模糊查询
            if (StringUtils.hasText(queryDTO.getRealName())) {
                predicates.add(criteriaBuilder.like(root.get("realName"), "%" + queryDTO.getRealName() + "%"));
            }
            
            // 手机号精确查询
            if (StringUtils.hasText(queryDTO.getPhone())) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), queryDTO.getPhone()));
            }
            
            // 部门ID
            if (StringUtils.hasText(queryDTO.getDepartmentId())) {
                predicates.add(criteriaBuilder.equal(root.get("departmentId"), queryDTO.getDepartmentId()));
            }
            
            // 状态
            if (queryDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 构建排序
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(queryDTO.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
            queryDTO.getSortBy()
        );
        
        // 分页查询
        Pageable pageable = PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize(), sort);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // 转换为DTO
        List<UserDetailDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
        
        return ResponsePageDataEntity.<UserDetailDTO>builder()
                .content(userDTOs)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .size(userPage.getSize())
                .number(userPage.getNumber() + 1) // 转换为1开始的页码
                .numberOfElements(userPage.getNumberOfElements())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }
    
    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.info("删除用户: {}", userId);
        
        User user = getUserEntityById(userId);
        user.setDeleted(true);
        user.setUpdatedBy(getCurrentUserId());
        userRepository.save(user);
        
        // 删除用户角色关联
        userRoleRepository.deleteByUserId(userId);
    }
    
    @Override
    @Transactional
    public void updateUserStatus(String userId, Integer status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        User user = getUserEntityById(userId);
        user.setStatus(status);
        user.setUpdatedBy(getCurrentUserId());
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void resetUserPassword(String userId, String newPassword) {
        log.info("重置用户密码: {}", userId);
        
        User user = getUserEntityById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(getCurrentUserId());
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void assignUserRoles(String userId, List<String> roleIds) {
        log.info("分配用户角色: userId={}, roleIds={}", userId, roleIds);
        
        // 验证用户存在
        getUserEntityById(userId);
        
        // 验证角色存在
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException("部分角色不存在");
        }
        
        // 创建用户角色关联
        String currentUserId = getCurrentUserId();
        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> UserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .createdBy(currentUserId)
                        .build())
                .collect(Collectors.toList());
        
        userRoleRepository.saveAll(userRoles);
    }
    
    @Override
    @Transactional
    public void removeUserRoles(String userId, List<String> roleIds) {
        log.info("移除用户角色: userId={}, roleIds={}", userId, roleIds);
        
        userRoleRepository.deleteByUserIdAndRoleIdIn(userId, roleIds);
    }
    
    private User getUserEntityById(String userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new DataNotFoundException("用户不存在"));
    }
    
    private UserDetailDTO convertToDetailDTO(User user) {
        // 获取用户角色
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        List<RoleDTO> roleDTOs = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            roleDTOs = roles.stream()
                    .map(this::convertToRoleDTO)
                    .collect(Collectors.toList());
        }
        
        return UserDetailDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .departmentId(user.getDepartmentId())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .version(user.getVersion())
                .roles(roleDTOs)
                .build();
    }
    
    private RoleDTO convertToRoleDTO(Role role) {
        // 获取角色权限
        List<PermissionDTO> permissionDTOs = role.getPermissions().stream()
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
    
    private String getCurrentUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}