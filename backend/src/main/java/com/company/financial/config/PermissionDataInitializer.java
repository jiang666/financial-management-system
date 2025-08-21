package com.company.financial.config;

import com.company.financial.entity.*;
import com.company.financial.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * 权限数据初始化器 - 添加API接口权限
 * 在基础数据初始化之后运行
 * 
 * @author System
 */
@Component
@Slf4j
@Order(2) // 在DataInitializer之后执行
public class PermissionDataInitializer implements CommandLineRunner {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RoleResourceRepository roleResourceRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeApiPermissions();
    }
    
    private void initializeApiPermissions() {
        try {
            log.info("开始初始化API接口权限...");
            
            // 检查是否已经初始化过API权限
            if (resourceRepository.findByResourceCodeAndDeleted("API_USER_LIST", 0) != null) {
                log.info("API权限已初始化，跳过初始化步骤");
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // 获取用户管理和角色管理的菜单
            Resource userMenu = resourceRepository.findByResourceCodeAndDeleted("USER_MANAGEMENT", 0);
            Resource roleMenu = resourceRepository.findByResourceCodeAndDeleted("ROLE_MANAGEMENT", 0);
            
            if (userMenu != null) {
                createUserApiPermissions(userMenu.getId(), currentTime);
            }
            
            if (roleMenu != null) {
                createRoleApiPermissions(roleMenu.getId(), currentTime);
            }
            
            // 创建资源管理API权限
            createResourceApiPermissions(currentTime);
            
            // 给超级管理员分配所有API权限
            assignApiPermissionsToAdmin();
            
            log.info("API接口权限初始化完成");
            
        } catch (Exception e) {
            log.error("API权限初始化失败", e);
        }
    }
    
    /**
     * 创建用户管理API权限
     */
    private void createUserApiPermissions(String parentId, long currentTime) {
        // 用户管理API接口权限
        ApiPermission[] userApis = {
            new ApiPermission("API_USER_LIST", "用户列表查询", "GET /v1/users", "分页查询用户列表"),
            new ApiPermission("API_USER_DETAIL", "用户详情查询", "GET /v1/users/{id}", "根据ID查询用户详情"),
            new ApiPermission("API_USER_CREATE", "创建用户", "POST /v1/users", "创建新用户"),
            new ApiPermission("API_USER_UPDATE", "更新用户", "PUT /v1/users/{id}", "更新用户信息"),
            new ApiPermission("API_USER_DELETE", "删除用户", "DELETE /v1/users/{id}", "删除单个用户"),
            new ApiPermission("API_USER_BATCH_DELETE", "批量删除用户", "DELETE /v1/users/batch", "批量删除用户"),
            new ApiPermission("API_USER_RESET_PASSWORD", "重置用户密码", "PUT /v1/users/{id}/password", "重置用户密码"),
            new ApiPermission("API_USER_UPDATE_STATUS", "更新用户状态", "PUT /v1/users/{id}/status", "更新用户状态"),
            new ApiPermission("API_USER_ASSIGN_ROLES", "分配用户角色", "PUT /v1/users/{id}/roles", "为用户分配角色"),
            new ApiPermission("API_USER_REMOVE_ROLES", "移除用户角色", "DELETE /v1/users/{id}/roles", "移除用户角色"),
            new ApiPermission("API_USER_EXPORT", "导出用户数据", "GET /v1/users/export", "导出用户数据")
        };
        
        for (int i = 0; i < userApis.length; i++) {
            createApiResource(userApis[i], parentId, i + 100, currentTime);
        }
    }
    
    /**
     * 创建角色管理API权限
     */
    private void createRoleApiPermissions(String parentId, long currentTime) {
        // 角色管理API接口权限
        ApiPermission[] roleApis = {
            new ApiPermission("API_ROLE_LIST", "角色列表查询", "GET /v1/roles", "分页查询角色列表"),
            new ApiPermission("API_ROLE_ALL", "查询所有角色", "GET /v1/roles/all", "查询所有角色列表"),
            new ApiPermission("API_ROLE_DETAIL", "角色详情查询", "GET /v1/roles/{id}", "根据ID查询角色详情"),
            new ApiPermission("API_ROLE_CREATE", "创建角色", "POST /v1/roles", "创建新角色"),
            new ApiPermission("API_ROLE_UPDATE", "更新角色", "PUT /v1/roles/{id}", "更新角色信息"),
            new ApiPermission("API_ROLE_DELETE", "删除角色", "DELETE /v1/roles/{id}", "删除单个角色"),
            new ApiPermission("API_ROLE_BATCH_DELETE", "批量删除角色", "DELETE /v1/roles/batch", "批量删除角色"),
            new ApiPermission("API_ROLE_UPDATE_STATUS", "更新角色状态", "PUT /v1/roles/{id}/status", "更新角色状态"),
            new ApiPermission("API_ROLE_CONFIG_PERMISSIONS", "配置角色权限", "PUT /v1/roles/{id}/permissions", "配置角色权限"),
            new ApiPermission("API_ROLE_GET_PERMISSIONS", "获取角色权限", "GET /v1/roles/{id}/permissions", "获取角色权限列表")
        };
        
        for (int i = 0; i < roleApis.length; i++) {
            createApiResource(roleApis[i], parentId, i + 200, currentTime);
        }
    }
    
    /**
     * 创建资源管理API权限
     */
    private void createResourceApiPermissions(long currentTime) {
        // 先创建资源管理菜单（如果不存在）
        Resource resourceMenu = resourceRepository.findByResourceCodeAndDeleted("RESOURCE_MANAGEMENT", 0);
        if (resourceMenu == null) {
            Resource systemRoot = resourceRepository.findByResourceCodeAndDeleted("SYSTEM_ROOT", 0);
            if (systemRoot != null) {
                resourceMenu = new Resource();
                resourceMenu.setId(UUID.randomUUID().toString());
                resourceMenu.setResourceCode("RESOURCE_MANAGEMENT");
                resourceMenu.setResourceName("资源管理");
                resourceMenu.setResourceType("submenu");
                resourceMenu.setParentId(systemRoot.getId());
                resourceMenu.setUrl("/system/resources");
                resourceMenu.setIcon("el-icon-menu");
                resourceMenu.setSortOrder(3);
                resourceMenu.setPath("/system/resources");
                resourceMenu.setLevel(2);
                resourceMenu.setDescription("资源管理页面");
                resourceMenu.setDeleted(0);
                resourceMenu.setCreateTime(currentTime);
                resourceMenu.setUpdateTime(currentTime);
                resourceMenu.setCreateBy("SYSTEM");
                resourceMenu.setUpdateBy("SYSTEM");
                
                resourceMenu = resourceRepository.save(resourceMenu);
            }
        }
        
        if (resourceMenu != null) {
            // 资源管理API接口权限
            ApiPermission[] resourceApis = {
                new ApiPermission("API_RESOURCE_TREE", "资源树查询", "GET /v1/resources/tree", "查询资源树结构"),
                new ApiPermission("API_RESOURCE_USER_MENUS", "用户菜单查询", "GET /v1/resources/user/{userId}/menus", "查询用户菜单"),
                new ApiPermission("API_RESOURCE_USER_PERMISSIONS", "用户权限查询", "GET /v1/resources/user/{userId}/permissions", "查询用户权限"),
                new ApiPermission("API_RESOURCE_USER_HAS_PERMISSION", "用户权限检查", "GET /v1/resources/user/{userId}/permission/{permission}", "检查用户是否有指定权限"),
                new ApiPermission("API_RESOURCE_BY_TYPE", "按类型查询资源", "GET /v1/resources/type/{type}", "按类型查询资源")
            };
            
            for (int i = 0; i < resourceApis.length; i++) {
                createApiResource(resourceApis[i], resourceMenu.getId(), i + 300, currentTime);
            }
        }
    }
    
    /**
     * 创建API资源
     */
    private void createApiResource(ApiPermission apiPermission, String parentId, int sortOrder, long currentTime) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID().toString());
        resource.setResourceCode(apiPermission.code);
        resource.setResourceName(apiPermission.name);
        resource.setResourceType("interface");
        resource.setParentId(parentId);
        resource.setUrl(apiPermission.url);
        resource.setSortOrder(sortOrder);
        resource.setLevel(3);
        resource.setDescription(apiPermission.description);
        resource.setDeleted(0);
        resource.setCreateTime(currentTime);
        resource.setUpdateTime(currentTime);
        resource.setCreateBy("SYSTEM");
        resource.setUpdateBy("SYSTEM");
        
        resourceRepository.save(resource);
    }
    
    /**
     * 给超级管理员分配所有API权限
     */
    private void assignApiPermissionsToAdmin() {
        Optional<Role> adminRoleOpt = roleRepository.findByRoleCodeAndDeleted("ADMIN", 0);
        if (adminRoleOpt.isPresent()) {
            Role adminRole = adminRoleOpt.get();
            
            // 获取所有interface类型的资源
            resourceRepository.findByResourceTypeAndDeletedOrderBySortOrderAsc("interface", 0)
                    .forEach(resource -> {
                        // 检查是否已经分配过这个权限
                        if (!roleResourceRepository.findByRoleIdAndResourceId(adminRole.getId(), resource.getId()).isPresent()) {
                            RoleResource roleResource = new RoleResource();
                            roleResource.setId(UUID.randomUUID().toString());
                            roleResource.setRoleId(adminRole.getId());
                            roleResource.setResourceId(resource.getId());
                            roleResource.setCreateTime(System.currentTimeMillis());
                            roleResource.setCreateBy("SYSTEM");
                            
                            roleResourceRepository.save(roleResource);
                        }
                    });
            
            log.info("已为超级管理员分配所有API权限");
        }
    }
    
    /**
     * API权限内部类
     */
    private static class ApiPermission {
        String code;
        String name;
        String url;
        String description;
        
        ApiPermission(String code, String name, String url, String description) {
            this.code = code;
            this.name = name;
            this.url = url;
            this.description = description;
        }
    }
}