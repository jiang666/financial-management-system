package com.company.financial.service.impl;

import com.company.financial.dto.user.*;
import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.entity.UserRole;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.repository.UserRoleRepository;
import com.company.financial.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author System
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Page<UserDetailDTO> findUsers(UserQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize());
        
        Page<User> userPage = userRepository.findUsersByConditions(
                queryDTO.getUsername(),
                queryDTO.getRealName(),
                queryDTO.getDepartment(),
                queryDTO.getStatus(),
                0,
                pageable
        );
        
        List<UserDetailDTO> userDetailDTOs = userPage.getContent().stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(userDetailDTOs, pageable, userPage.getTotalElements());
    }
    
    @Override
    public UserDetailDTO findUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }
        
        return convertToDetailDTO(user);
    }
    
    @Override
    @Transactional
    public UserDetailDTO createUser(UserCreateDTO createDTO) {
        try {
            log.info("开始创建用户: {}", createDTO.getUsername());
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsernameAndDeleted(createDTO.getUsername(), 0)) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (StringUtils.hasText(createDTO.getEmail()) && 
                userRepository.existsByEmailAndDeleted(createDTO.getEmail(), 0)) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 创建用户
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(createDTO.getUsername());
            user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
            user.setRealName(createDTO.getRealName());
            user.setEmail(createDTO.getEmail());
            user.setPhone(createDTO.getPhone());
            user.setDepartment(createDTO.getDepartment());
            user.setDepartmentId(createDTO.getDepartmentId());
            // 创建用户时不设置岗位，需要通过部门分配功能来设置
            // user.setPosition(createDTO.getPosition());
            // 如果未指定状态，默认设置为ACTIVE
            user.setStatus(StringUtils.hasText(createDTO.getStatus()) ? createDTO.getStatus() : "ACTIVE");
            user.setDeleted(0);
            
            long currentTime = System.currentTimeMillis();
            user.setCreateTime(currentTime);
            user.setUpdateTime(currentTime);
            user.setCreateBy("system"); // TODO: 从当前登录用户获取
            user.setUpdateBy("system");
            
            log.info("保存用户到数据库");
            user = userRepository.save(user);
            log.info("用户保存成功，ID: {}", user.getId());
            
            // 分配角色
            if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
                log.info("分配角色: {}", createDTO.getRoleIds());
                assignRoles(user.getId(), createDTO.getRoleIds());
            }
            
            log.info("转换为DetailDTO");
            UserDetailDTO result = convertToDetailDTO(user);
            log.info("用户创建完成");
            return result;
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建用户失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public UserDetailDTO updateUser(UserUpdateDTO updateDTO) {
        User user = userRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }
        
        // 检查邮箱是否已存在（排除当前用户）
        if (StringUtils.hasText(updateDTO.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmailAndDeleted(updateDTO.getEmail(), 0);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(updateDTO.getId())) {
                throw new RuntimeException("邮箱已存在");
            }
        }
        
        // 更新用户信息
        user.setRealName(updateDTO.getRealName());
        user.setEmail(updateDTO.getEmail());
        user.setPhone(updateDTO.getPhone());
        user.setDepartment(updateDTO.getDepartment());
        user.setDepartmentId(updateDTO.getDepartmentId());
        user.setStatus(updateDTO.getStatus());
        user.setUpdateTime(System.currentTimeMillis());
        user.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        user = userRepository.save(user);
        
        // 更新角色
        if (updateDTO.getRoleIds() != null) {
            userRoleRepository.deleteByUserId(user.getId());
            if (!updateDTO.getRoleIds().isEmpty()) {
                assignRoles(user.getId(), updateDTO.getRoleIds());
            }
        }
        
        return convertToDetailDTO(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 逻辑删除
        user.setDeleted(1);
        user.setUpdateTime(System.currentTimeMillis());
        user.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        userRepository.save(user);
        
        // 删除用户角色关联
        userRoleRepository.deleteByUserId(id);
    }
    
    @Override
    @Transactional
    public void deleteUsers(List<String> ids) {
        for (String id : ids) {
            deleteUser(id);
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(System.currentTimeMillis());
        user.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateStatus(String id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }
        
        user.setStatus(status);
        user.setUpdateTime(System.currentTimeMillis());
        user.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        long currentTime = System.currentTimeMillis();
        
        for (String roleId : roleIds) {
            if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(currentTime);
                userRole.setCreateBy("system"); // TODO: 从当前登录用户获取
                
                userRoleRepository.save(userRole);
            }
        }
    }
    
    @Override
    @Transactional
    public void removeRoles(String userId, List<String> roleIds) {
        for (String roleId : roleIds) {
            userRoleRepository.deleteByUserId(userId);
        }
    }
    
    @Override
    public List<UserDetailDTO> exportUsers(UserQueryDTO queryDTO) {
        // 设置大的分页大小以获取所有数据
        queryDTO.setPage(0);
        queryDTO.setSize(Integer.MAX_VALUE);
        
        Page<UserDetailDTO> page = findUsers(queryDTO);
        return page.getContent();
    }
    
    /**
     * 转换为详情DTO
     */
    private UserDetailDTO convertToDetailDTO(User user) {
        try {
            log.debug("开始转换用户DTO，用户ID: {}", user.getId());
            
            UserDetailDTO dto = new UserDetailDTO();
            BeanUtils.copyProperties(user, dto);
            
            // 手动映射字段名不一致的属性
            dto.setLastLoginAt(user.getLastLoginTime());
            dto.setLastLoginIp(user.getLastLoginIp());
            
            // 查询用户角色
            log.debug("查询用户角色，用户ID: {}", user.getId());
            List<Role> roles;
            try {
                roles = roleRepository.findRolesByUserId(user.getId());
                log.debug("查询到 {} 个角色", roles.size());
            } catch (Exception e) {
                log.error("查询用户角色失败，用户ID: {}, 错误: {}", user.getId(), e.getMessage());
                roles = new ArrayList<>();
            }
            
            List<UserDetailDTO.RoleInfo> roleInfos = roles.stream()
                    .map(role -> {
                        UserDetailDTO.RoleInfo roleInfo = new UserDetailDTO.RoleInfo();
                        roleInfo.setId(role.getId());
                        roleInfo.setRoleCode(role.getRoleCode());
                        roleInfo.setName(role.getName());
                        roleInfo.setDescription(role.getDescription());
                        return roleInfo;
                    })
                    .collect(Collectors.toList());
            
            dto.setRoles(roleInfos);
            
            log.debug("用户DTO转换完成");
            return dto;
        } catch (Exception e) {
            log.error("转换用户DTO失败，用户ID: {}, 错误: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("转换用户DTO失败: " + e.getMessage(), e);
        }
    }
}