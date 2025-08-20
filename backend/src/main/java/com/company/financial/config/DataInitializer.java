package com.company.financial.config;

import com.company.financial.entity.Permission;
import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.repository.PermissionRepository;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    @Transactional
    public CommandLineRunner initData(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      PermissionRepository permissionRepository) {
        return args -> {
            log.info("开始初始化数据...");
            
            // 初始化权限
            if (permissionRepository.count() == 0) {
                log.info("初始化权限数据");
                
                Permission p1 = createPermission("USER_VIEW", "查看用户", "查看用户列表和详情");
                Permission p2 = createPermission("USER_CREATE", "创建用户", "创建新用户");
                Permission p3 = createPermission("USER_UPDATE", "更新用户", "更新用户信息");
                Permission p4 = createPermission("USER_DELETE", "删除用户", "删除用户");
                Permission p5 = createPermission("VOUCHER_VIEW", "查看凭证", "查看凭证列表和详情");
                Permission p6 = createPermission("VOUCHER_CREATE", "创建凭证", "创建新凭证");
                Permission p7 = createPermission("VOUCHER_UPDATE", "更新凭证", "更新凭证信息");
                Permission p8 = createPermission("VOUCHER_DELETE", "删除凭证", "删除凭证");
                Permission p9 = createPermission("VOUCHER_AUDIT", "审核凭证", "审核凭证");
                Permission p10 = createPermission("REPORT_VIEW", "查看报表", "查看各类财务报表");
                Permission p11 = createPermission("REPORT_EXPORT", "导出报表", "导出报表数据");
                Permission p12 = createPermission("SYSTEM_MANAGE", "系统管理", "系统配置管理");
                
                permissionRepository.saveAll(Arrays.asList(
                    p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12
                ));
            }
            
            // 初始化角色
            if (roleRepository.count() == 0) {
                log.info("初始化角色数据");
                
                Permission p1 = permissionRepository.findByCode("USER_VIEW").orElse(null);
                Permission p2 = permissionRepository.findByCode("USER_CREATE").orElse(null);
                Permission p3 = permissionRepository.findByCode("USER_UPDATE").orElse(null);
                Permission p4 = permissionRepository.findByCode("USER_DELETE").orElse(null);
                Permission p5 = permissionRepository.findByCode("VOUCHER_VIEW").orElse(null);
                Permission p6 = permissionRepository.findByCode("VOUCHER_CREATE").orElse(null);
                Permission p7 = permissionRepository.findByCode("VOUCHER_UPDATE").orElse(null);
                Permission p8 = permissionRepository.findByCode("VOUCHER_DELETE").orElse(null);
                Permission p9 = permissionRepository.findByCode("VOUCHER_AUDIT").orElse(null);
                Permission p10 = permissionRepository.findByCode("REPORT_VIEW").orElse(null);
                Permission p11 = permissionRepository.findByCode("REPORT_EXPORT").orElse(null);
                Permission p12 = permissionRepository.findByCode("SYSTEM_MANAGE").orElse(null);
                
                // 系统管理员
                Role adminRole = createRole("ADMIN", "系统管理员", "拥有所有权限");
                adminRole.setPermissions(new HashSet<>(Arrays.asList(
                    p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12
                )));
                
                // 会计
                Role accountantRole = createRole("ACCOUNTANT", "会计", "负责凭证录入");
                accountantRole.setPermissions(new HashSet<>(Arrays.asList(
                    p5, p6, p7, p10
                )));
                
                // 财务主管
                Role supervisorRole = createRole("FINANCE_SUPERVISOR", "财务主管", "负责凭证审核");
                supervisorRole.setPermissions(new HashSet<>(Arrays.asList(
                    p5, p9, p10, p11
                )));
                
                // 财务经理
                Role managerRole = createRole("FINANCE_MANAGER", "财务经理", "负责财务管理");
                managerRole.setPermissions(new HashSet<>(Arrays.asList(
                    p5, p6, p7, p8, p9, p10, p11
                )));
                
                // 出纳
                Role cashierRole = createRole("CASHIER", "出纳", "负责现金管理");
                cashierRole.setPermissions(new HashSet<>(Arrays.asList(
                    p5, p10
                )));
                
                // 查看者
                Role viewerRole = createRole("VIEWER", "查看者", "只能查看");
                viewerRole.setPermissions(new HashSet<>(Arrays.asList(
                    p5, p10
                )));
                
                roleRepository.saveAll(Arrays.asList(
                    adminRole, accountantRole, supervisorRole, 
                    managerRole, cashierRole, viewerRole
                ));
            }
            
            // 初始化管理员用户
            if (!userRepository.existsByUsername("admin")) {
                log.info("初始化管理员用户");
                
                Role adminRole = roleRepository.findByCode("ADMIN").orElse(null);
                
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@company.com");
                admin.setRealName("系统管理员");
                admin.setPhone("13800000000");
                admin.setStatus(1);
                admin.setCreatedBy("system");
                admin.setUpdatedBy("system");
                
                if (adminRole != null) {
                    admin.addRole(adminRole);
                }
                
                userRepository.save(admin);
            }
            
            log.info("数据初始化完成");
        };
    }
    
    private Permission createPermission(String code, String name, String description) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setDescription(description);
        permission.setCreatedBy("system");
        permission.setUpdatedBy("system");
        return permission;
    }
    
    private Role createRole(String code, String name, String description) {
        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setCreatedBy("system");
        role.setUpdatedBy("system");
        return role;
    }
}