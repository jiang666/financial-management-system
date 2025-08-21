package com.company.financial.service.impl;

import com.company.financial.dto.role.*;
import com.company.financial.entity.Resource;
import com.company.financial.entity.Role;
import com.company.financial.entity.RoleResource;
import com.company.financial.repository.ResourceRepository;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.RoleResourceRepository;
import com.company.financial.repository.UserRoleRepository;
import com.company.financial.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 
 * @author System
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private RoleResourceRepository roleResourceRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Override
    public Page<RoleDetailDTO> findRoles(RoleQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize());
        
        Page<Role> rolePage = roleRepository.findRolesByConditions(
                queryDTO.getRoleCode(),
                queryDTO.getName(),
                queryDTO.getStatus(),
                0,
                pageable
        );
        
        List<RoleDetailDTO> roleDetailDTOs = rolePage.getContent().stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(roleDetailDTOs, pageable, rolePage.getTotalElements());
    }
    
    @Override
    public List<RoleDetailDTO> findAllRoles() {
        List<Role> roles = roleRepository.findByStatusAndDeletedOrderByCreateTime("ACTIVE", 0);
        return roles.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleDetailDTO findRoleById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        if (role.getDeleted() == 1) {
            throw new RuntimeException("角色已被删除");
        }
        
        return convertToDetailDTO(role);
    }
    
    @Override
    @Transactional
    public RoleDetailDTO createRole(RoleCreateDTO createDTO) {
        try {
            log.info("开始创建角色，参数: {}", createDTO);
            
            // 检查角色编码是否已存在
            log.debug("检查角色编码是否存在: {}", createDTO.getRoleCode());
            if (roleRepository.existsByRoleCodeAndDeleted(createDTO.getRoleCode(), 0)) {
                throw new RuntimeException("角色编码已存在");
            }
            
            // 检查角色名称是否已存在
            log.debug("检查角色名称是否存在: {}", createDTO.getName());
            if (roleRepository.existsByNameAndDeleted(createDTO.getName(), 0)) {
                throw new RuntimeException("角色名称已存在");
            }
            
            // 创建角色
            log.debug("创建新角色实体");
            Role role = new Role();
            role.setId(UUID.randomUUID().toString());
            role.setRoleCode(createDTO.getRoleCode());
            role.setName(createDTO.getName());
            role.setDescription(createDTO.getDescription());
            // 如果未指定状态，默认设置为ACTIVE
            String status = StringUtils.hasText(createDTO.getStatus()) ? createDTO.getStatus() : "ACTIVE";
            role.setStatus(status);
            log.debug("设置角色状态: {}", status);
            
            role.setCanDelete(createDTO.getCanDelete() != null ? createDTO.getCanDelete() : 1);
            role.setCanModify(createDTO.getCanModify() != null ? createDTO.getCanModify() : 1);
            role.setUserCount(0);
            role.setDeleted(0);
            
            long currentTime = System.currentTimeMillis();
            role.setCreateTime(currentTime);
            role.setUpdateTime(currentTime);
            role.setCreateBy("system"); // TODO: 从当前登录用户获取
            role.setUpdateBy("system");
            
            log.debug("保存角色到数据库");
            role = roleRepository.save(role);
            log.info("角色保存成功，ID: {}", role.getId());
            
            // 配置权限
            if (createDTO.getResourceIds() != null && !createDTO.getResourceIds().isEmpty()) {
                log.debug("配置角色权限，权限数量: {}", createDTO.getResourceIds().size());
                configurePermissions(role.getId(), createDTO.getResourceIds());
            }
            
            log.debug("转换角色DTO");
            RoleDetailDTO result = convertToDetailDTO(role);
            log.info("角色创建完成，返回结果: {}", result.getName());
            
            return result;
        } catch (Exception e) {
            log.error("创建角色失败，详细错误信息: ", e);
            throw new RuntimeException("创建角色失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public RoleDetailDTO updateRole(RoleUpdateDTO updateDTO) {
        Role role = roleRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        if (role.getDeleted() == 1) {
            throw new RuntimeException("角色已被删除");
        }
        
        if (role.getCanModify() == 0) {
            throw new RuntimeException("该角色不允许修改");
        }
        
        // 检查角色名称是否已存在（排除当前角色）
        Optional<Role> existingRole = roleRepository.findByNameAndDeleted(updateDTO.getName(), 0);
        if (existingRole.isPresent() && !existingRole.get().getId().equals(updateDTO.getId())) {
            throw new RuntimeException("角色名称已存在");
        }
        
        // 更新角色信息
        role.setName(updateDTO.getName());
        role.setDescription(updateDTO.getDescription());
        role.setStatus(updateDTO.getStatus());
        role.setUpdateTime(System.currentTimeMillis());
        role.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        role = roleRepository.save(role);
        
        // 更新权限
        if (updateDTO.getResourceIds() != null) {
            roleResourceRepository.deleteByRoleId(role.getId());
            if (!updateDTO.getResourceIds().isEmpty()) {
                configurePermissions(role.getId(), updateDTO.getResourceIds());
            }
        }
        
        return convertToDetailDTO(role);
    }
    
    @Override
    @Transactional
    public void deleteRole(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        if (role.getCanDelete() == 0) {
            throw new RuntimeException("该角色不允许删除");
        }
        
        // 检查是否有用户使用该角色
        if (!userRoleRepository.findByRoleId(id).isEmpty()) {
            throw new RuntimeException("该角色已被用户使用，无法删除");
        }
        
        // 逻辑删除
        role.setDeleted(1);
        role.setUpdateTime(System.currentTimeMillis());
        role.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        roleRepository.save(role);
        
        // 删除角色资源关联
        roleResourceRepository.deleteByRoleId(id);
    }
    
    @Override
    @Transactional
    public void deleteRoles(List<String> ids) {
        for (String id : ids) {
            deleteRole(id);
        }
    }
    
    @Override
    @Transactional
    public void updateStatus(String id, String status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        if (role.getDeleted() == 1) {
            throw new RuntimeException("角色已被删除");
        }
        
        role.setStatus(status);
        role.setUpdateTime(System.currentTimeMillis());
        role.setUpdateBy("system"); // TODO: 从当前登录用户获取
        
        roleRepository.save(role);
    }
    
    @Override
    @Transactional
    public void configurePermissions(String roleId, List<String> resourceIds) {
        long currentTime = System.currentTimeMillis();
        
        for (String resourceId : resourceIds) {
            if (!roleResourceRepository.existsByRoleIdAndResourceId(roleId, resourceId)) {
                RoleResource roleResource = new RoleResource();
                roleResource.setId(UUID.randomUUID().toString());
                roleResource.setRoleId(roleId);
                roleResource.setResourceId(resourceId);
                roleResource.setCreateTime(currentTime);
                roleResource.setCreateBy("system"); // TODO: 从当前登录用户获取
                
                roleResourceRepository.save(roleResource);
            }
        }
    }
    
    @Override
    public List<String> getRolePermissions(String roleId) {
        List<RoleResource> roleResources = roleResourceRepository.findByRoleId(roleId);
        return roleResources.stream()
                .map(RoleResource::getResourceId)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为详情DTO
     */
    private RoleDetailDTO convertToDetailDTO(Role role) {
        try {
            log.debug("开始转换角色DTO，角色ID: {}", role.getId());
            RoleDetailDTO dto = new RoleDetailDTO();
            BeanUtils.copyProperties(role, dto);
            log.debug("基本属性拷贝完成");
            
            // 动态计算实际用户数量
            try {
                int actualUserCount = userRoleRepository.findByRoleId(role.getId()).size();
                dto.setUserCount(actualUserCount);
                log.debug("用户数量计算完成: {}", actualUserCount);
            } catch (Exception userCountException) {
                log.warn("计算用户数量失败，使用默认值0: {}", userCountException.getMessage());
                dto.setUserCount(0);
            }
            
            // 查询角色资源
            try {
                List<Resource> resources = resourceRepository.findResourcesByRoleId(role.getId());
                List<RoleDetailDTO.ResourceInfo> resourceInfos = resources.stream()
                        .map(resource -> {
                            RoleDetailDTO.ResourceInfo resourceInfo = new RoleDetailDTO.ResourceInfo();
                            resourceInfo.setId(resource.getId());
                            resourceInfo.setResourceCode(resource.getResourceCode());
                            resourceInfo.setResourceName(resource.getResourceName());
                            resourceInfo.setResourceType(resource.getResourceType());
                            resourceInfo.setParentId(resource.getParentId());
                            resourceInfo.setUrl(resource.getUrl());
                            resourceInfo.setIcon(resource.getIcon());
                            resourceInfo.setSortOrder(resource.getSortOrder());
                            resourceInfo.setLevel(resource.getLevel());
                            resourceInfo.setDescription(resource.getDescription());
                            return resourceInfo;
                        })
                        .collect(Collectors.toList());
                
                dto.setResources(resourceInfos);
            } catch (Exception resourceException) {
                log.warn("查询角色 {} 的资源信息失败: {}", role.getId(), resourceException.getMessage());
                dto.setResources(new ArrayList<>());
            }
            log.debug("角色DTO转换完成");
            
            return dto;
        } catch (Exception e) {
            log.error("转换角色DTO失败，角色ID: {}, 错误: ", role.getId(), e);
            throw new RuntimeException("角色信息转换失败: " + e.getMessage());
        }
    }
}