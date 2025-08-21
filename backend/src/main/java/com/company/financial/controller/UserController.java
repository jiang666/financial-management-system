package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.common.response.ResponsePageData;
import com.company.financial.dto.user.*;
import com.company.financial.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author System
 */
@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表
     * 
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    @GetMapping
    public ResponseEntity<ResponsePageData<UserDetailDTO>> getUsers(UserQueryDTO queryDTO) {
        try {
            Page<UserDetailDTO> page = userService.findUsers(queryDTO);
            return ResponseEntity.ok(ResponsePageData.success(page));
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ResponseEntity.ok(ResponsePageData.success("查询失败: " + e.getMessage(), Page.empty()));
        }
    }
    
    /**
     * 根据ID查询用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserDetailDTO>> getUserById(@PathVariable String id) {
        try {
            UserDetailDTO user = userService.findUserById(id);
            return ResponseEntity.ok(ResponseData.success(user));
        } catch (Exception e) {
            log.error("查询用户详情失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建用户
     * 
     * @param createDTO 创建信息
     * @return 用户详情
     */
    @PostMapping
    public ResponseEntity<ResponseData<UserDetailDTO>> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        try {
            log.info("接收到用户创建请求: {}", createDTO);
            UserDetailDTO user = userService.createUser(createDTO);
            return ResponseEntity.ok(ResponseData.success("用户创建成功", user));
        } catch (Exception e) {
            log.error("创建用户失败，详细错误信息: ", e);
            return ResponseEntity.ok(ResponseData.error("创建失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<ResponseData<String>> testEndpoint(@RequestBody Map<String, Object> testData) {
        try {
            log.info("收到测试请求: {}", testData);
            
            // 测试创建一个基本用户
            if (testData.containsKey("testUserCreation")) {
                try {
                    UserCreateDTO createDTO = new UserCreateDTO();
                    createDTO.setUsername("testuser");
                    createDTO.setPassword("123456");
                    createDTO.setRealName("测试用户");
                    
                    UserDetailDTO user = userService.createUser(createDTO);
                    return ResponseEntity.ok(ResponseData.success("用户创建测试成功", user.getUsername()));
                } catch (Exception e) {
                    log.error("用户创建测试失败", e);
                    return ResponseEntity.ok(ResponseData.error("用户创建测试失败: " + e.getMessage()));
                }
            }
            
            return ResponseEntity.ok(ResponseData.success("测试成功", "数据接收正常"));
        } catch (Exception e) {
            log.error("测试失败", e);
            return ResponseEntity.ok(ResponseData.error("测试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param updateDTO 更新信息
     * @return 用户详情
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<UserDetailDTO>> updateUser(@PathVariable String id, 
                                                                @Valid @RequestBody UserUpdateDTO updateDTO) {
        try {
            updateDTO.setId(id);
            UserDetailDTO user = userService.updateUser(updateDTO);
            return ResponseEntity.ok(ResponseData.success("用户更新成功", user));
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Object>> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ResponseData.success("用户删除成功"));
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除用户
     * 
     * @param request 用户ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseData<Object>> deleteUsers(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> ids = request.get("ids");
            userService.deleteUsers(ids);
            return ResponseEntity.ok(ResponseData.success("用户批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除用户失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param request 密码信息
     * @return 响应结果
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ResponseData<Object>> resetPassword(@PathVariable String id, 
                                                           @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok(ResponseData.success("密码重置成功"));
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ResponseEntity.ok(ResponseData.error("重置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param request 状态信息
     * @return 响应结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseData<Object>> updateStatus(@PathVariable String id, 
                                                          @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            userService.updateStatus(id, status);
            return ResponseEntity.ok(ResponseData.success("状态更新成功"));
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 为用户分配角色
     * 
     * @param id 用户ID
     * @param request 角色ID列表
     * @return 响应结果
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<ResponseData<Object>> assignRoles(@PathVariable String id, 
                                                         @RequestBody Map<String, List<String>> request) {
        try {
            List<String> roleIds = request.get("roleIds");
            userService.assignRoles(id, roleIds);
            return ResponseEntity.ok(ResponseData.success("角色分配成功"));
        } catch (Exception e) {
            log.error("角色分配失败", e);
            return ResponseEntity.ok(ResponseData.error("分配失败: " + e.getMessage()));
        }
    }
    
    /**
     * 移除用户角色
     * 
     * @param id 用户ID
     * @param request 角色ID列表
     * @return 响应结果
     */
    @DeleteMapping("/{id}/roles")
    public ResponseEntity<ResponseData<Object>> removeRoles(@PathVariable String id, 
                                                         @RequestBody Map<String, List<String>> request) {
        try {
            List<String> roleIds = request.get("roleIds");
            userService.removeRoles(id, roleIds);
            return ResponseEntity.ok(ResponseData.success("角色移除成功"));
        } catch (Exception e) {
            log.error("角色移除失败", e);
            return ResponseEntity.ok(ResponseData.error("移除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出用户数据
     * 
     * @param queryDTO 查询条件
     * @return 用户数据
     */
    @GetMapping("/export")
    public ResponseEntity<ResponseData<List<UserDetailDTO>>> exportUsers(UserQueryDTO queryDTO) {
        try {
            List<UserDetailDTO> users = userService.exportUsers(queryDTO);
            return ResponseEntity.ok(ResponseData.success("导出成功", users));
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
            return ResponseEntity.ok(ResponseData.error("导出失败: " + e.getMessage()));
        }
    }
}