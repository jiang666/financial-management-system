package com.company.financial.config;

import com.company.financial.entity.*;
import com.company.financial.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 数据初始化器
 * 创建默认管理员账户和基础权限数据
 * 
 * @author System
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleResourceRepository roleResourceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeData();
    }
    
    private void initializeData() {
        try {
            // 检查是否已有管理员
            if (userRepository.findByUsernameAndDeleted("admin", 0).isPresent()) {
                log.info("数据已初始化，跳过初始化步骤");
                return;
            }
            
            log.info("开始初始化基础数据...");
            
            // 创建超级管理员角色
            Role adminRole = createAdminRole();
            
            // 创建基础资源
            Resource systemResource = createSystemResources();
            
            // 创建管理员用户
            User adminUser = createAdminUser();
            
            // 分配角色给管理员
            assignRoleToUser(adminUser, adminRole);
            
            // 分配权限给角色
            assignResourceToRole(adminRole, systemResource);
            
            log.info("基础数据初始化完成");
            
        } catch (Exception e) {
            log.error("数据初始化失败", e);
        }
    }
    
    private Role createAdminRole() {
        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setRoleCode("ADMIN");
        role.setName("超级管理员");
        role.setDescription("系统超级管理员，拥有所有权限");
        role.setStatus("ACTIVE");
        role.setCanDelete(0); // 不可删除
        role.setCanModify(0); // 不可修改
        role.setUserCount(0);
        role.setDeleted(0);
        role.setCreateTime(System.currentTimeMillis());
        role.setUpdateTime(System.currentTimeMillis());
        role.setCreateBy("SYSTEM");
        role.setUpdateBy("SYSTEM");
        
        return roleRepository.save(role);
    }
    
    private Resource createSystemResources() {
        long currentTime = System.currentTimeMillis();
        
        // 创建系统管理根节点
        Resource systemRoot = new Resource();
        systemRoot.setId(UUID.randomUUID().toString());
        systemRoot.setResourceCode("SYSTEM_ROOT");
        systemRoot.setResourceName("系统管理");
        systemRoot.setResourceType("menu-grouping");
        systemRoot.setParentId("0");
        systemRoot.setUrl("/system");
        systemRoot.setIcon("el-icon-setting");
        systemRoot.setSortOrder(1);
        systemRoot.setPath("/system");
        systemRoot.setLevel(1);
        systemRoot.setDescription("系统管理根节点");
        systemRoot.setDeleted(0);
        systemRoot.setCreateTime(currentTime);
        systemRoot.setUpdateTime(currentTime);
        systemRoot.setCreateBy("SYSTEM");
        systemRoot.setUpdateBy("SYSTEM");
        
        systemRoot = resourceRepository.save(systemRoot);
        
        // 创建用户管理子菜单
        Resource userMenu = new Resource();
        userMenu.setId(UUID.randomUUID().toString());
        userMenu.setResourceCode("USER_MANAGEMENT");
        userMenu.setResourceName("用户管理");
        userMenu.setResourceType("submenu");
        userMenu.setParentId(systemRoot.getId());
        userMenu.setUrl("/system/users");
        userMenu.setIcon("el-icon-user");
        userMenu.setSortOrder(1);
        userMenu.setPath("/system/users");
        userMenu.setLevel(2);
        userMenu.setDescription("用户管理页面");
        userMenu.setDeleted(0);
        userMenu.setCreateTime(currentTime);
        userMenu.setUpdateTime(currentTime);
        userMenu.setCreateBy("SYSTEM");
        userMenu.setUpdateBy("SYSTEM");
        
        userMenu = resourceRepository.save(userMenu);
        
        // 创建用户管理操作权限
        createUserOperationResources(userMenu.getId(), currentTime);
        
        // 创建角色管理子菜单
        Resource roleMenu = new Resource();
        roleMenu.setId(UUID.randomUUID().toString());
        roleMenu.setResourceCode("ROLE_MANAGEMENT");
        roleMenu.setResourceName("角色管理");
        roleMenu.setResourceType("submenu");
        roleMenu.setParentId(systemRoot.getId());
        roleMenu.setUrl("/system/roles");
        roleMenu.setIcon("el-icon-s-custom");
        roleMenu.setSortOrder(2);
        roleMenu.setPath("/system/roles");
        roleMenu.setLevel(2);
        roleMenu.setDescription("角色管理页面");
        roleMenu.setDeleted(0);
        roleMenu.setCreateTime(currentTime);
        roleMenu.setUpdateTime(currentTime);
        roleMenu.setCreateBy("SYSTEM");
        roleMenu.setUpdateBy("SYSTEM");
        
        roleMenu = resourceRepository.save(roleMenu);
        
        // 创建角色管理操作权限
        createRoleOperationResources(roleMenu.getId(), currentTime);
        
        return systemRoot;
    }
    
    private void createUserOperationResources(String parentId, long currentTime) {
        String[] operations = {"新增", "编辑", "删除", "查看详情", "重置密码", "分配角色"};
        String[] codes = {"USER_ADD", "USER_EDIT", "USER_DELETE", "USER_VIEW", "USER_RESET_PWD", "USER_ASSIGN_ROLE"};
        
        for (int i = 0; i < operations.length; i++) {
            Resource resource = new Resource();
            resource.setId(UUID.randomUUID().toString());
            resource.setResourceCode(codes[i]);
            resource.setResourceName(operations[i]);
            resource.setResourceType("crud");
            resource.setParentId(parentId);
            resource.setSortOrder(i + 1);
            resource.setLevel(3);
            resource.setDescription("用户管理-" + operations[i]);
            resource.setDeleted(0);
            resource.setCreateTime(currentTime);
            resource.setUpdateTime(currentTime);
            resource.setCreateBy("SYSTEM");
            resource.setUpdateBy("SYSTEM");
            
            resourceRepository.save(resource);
        }
    }
    
    private void createRoleOperationResources(String parentId, long currentTime) {
        String[] operations = {"新增", "编辑", "删除", "查看详情", "配置权限"};
        String[] codes = {"ROLE_ADD", "ROLE_EDIT", "ROLE_DELETE", "ROLE_VIEW", "ROLE_CONFIG_PERMISSION"};
        
        for (int i = 0; i < operations.length; i++) {
            Resource resource = new Resource();
            resource.setId(UUID.randomUUID().toString());
            resource.setResourceCode(codes[i]);
            resource.setResourceName(operations[i]);
            resource.setResourceType("crud");
            resource.setParentId(parentId);
            resource.setSortOrder(i + 1);
            resource.setLevel(3);
            resource.setDescription("角色管理-" + operations[i]);
            resource.setDeleted(0);
            resource.setCreateTime(currentTime);
            resource.setUpdateTime(currentTime);
            resource.setCreateBy("SYSTEM");
            resource.setUpdateBy("SYSTEM");
            
            resourceRepository.save(resource);
        }
    }
    
    private User createAdminUser() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRealName("系统管理员");
        user.setEmail("admin@system.com");
        user.setPhone("13800000000");
        user.setDepartment("信息技术部");
        user.setPosition("系统管理员");
        user.setStatus("ACTIVE");
        user.setDeleted(0);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateBy("SYSTEM");
        user.setUpdateBy("SYSTEM");
        
        return userRepository.save(user);
    }
    
    private void assignRoleToUser(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreateTime(System.currentTimeMillis());
        userRole.setCreateBy("SYSTEM");
        
        userRoleRepository.save(userRole);
    }
    
    private void assignResourceToRole(Role role, Resource resource) {
        // 递归分配所有资源权限给超级管理员角色
        assignResourceToRoleRecursive(role.getId(), resource.getId());
        
        // 获取所有子资源
        resourceRepository.findByParentIdAndDeletedOrderBySortOrderAsc(resource.getId(), 0)
                .forEach(childResource -> assignResourceToRole(role, childResource));
    }
    
    private void assignResourceToRoleRecursive(String roleId, String resourceId) {
        RoleResource roleResource = new RoleResource();
        roleResource.setId(UUID.randomUUID().toString());
        roleResource.setRoleId(roleId);
        roleResource.setResourceId(resourceId);
        roleResource.setCreateTime(System.currentTimeMillis());
        roleResource.setCreateBy("SYSTEM");
        
        roleResourceRepository.save(roleResource);
    }
}