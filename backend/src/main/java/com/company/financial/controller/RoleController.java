package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.common.response.ResponsePageData;
import com.company.financial.dto.role.*;
import com.company.financial.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 * 
 * @author System
 */
@RestController
@RequestMapping("/v1/roles")
@Slf4j
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 分页查询角色列表
     * 
     * @param queryDTO 查询条件
     * @return 角色分页数据
     */
    @GetMapping
    public ResponseEntity<ResponsePageData<RoleDetailDTO>> getRoles(RoleQueryDTO queryDTO) {
        try {
            Page<RoleDetailDTO> page = roleService.findRoles(queryDTO);
            return ResponseEntity.ok(ResponsePageData.success(page));
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return ResponseEntity.ok(ResponsePageData.success("查询失败: " + e.getMessage(), Page.empty()));
        }
    }
    
    /**
     * 查询所有角色列表
     * 
     * @return 角色列表
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseData<List<RoleDetailDTO>>> getAllRoles() {
        try {
            List<RoleDetailDTO> roles = roleService.findAllRoles();
            return ResponseEntity.ok(ResponseData.success(roles));
        } catch (Exception e) {
            log.error("查询所有角色失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID查询角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<RoleDetailDTO>> getRoleById(@PathVariable String id) {
        try {
            RoleDetailDTO role = roleService.findRoleById(id);
            return ResponseEntity.ok(ResponseData.success(role));
        } catch (Exception e) {
            log.error("查询角色详情失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建角色
     * 
     * @param createDTO 创建信息
     * @return 角色详情
     */
    @PostMapping
    public ResponseEntity<ResponseData<RoleDetailDTO>> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        try {
            log.info("接收到角色创建请求: {}", createDTO);
            RoleDetailDTO role = roleService.createRole(createDTO);
            return ResponseEntity.ok(ResponseData.success("角色创建成功", role));
        } catch (Exception e) {
            log.error("创建角色失败，详细错误信息: ", e);
            return ResponseEntity.ok(ResponseData.error("创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新角色
     * 
     * @param id 角色ID
     * @param updateDTO 更新信息
     * @return 角色详情
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<RoleDetailDTO>> updateRole(@PathVariable String id, 
                                                                @Valid @RequestBody RoleUpdateDTO updateDTO) {
        try {
            updateDTO.setId(id);
            RoleDetailDTO role = roleService.updateRole(updateDTO);
            return ResponseEntity.ok(ResponseData.success("角色更新成功", role));
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除角色
     * 
     * @param id 角色ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Object>> deleteRole(@PathVariable String id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ResponseData.success("角色删除成功"));
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除角色
     * 
     * @param request 角色ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseData<Object>> deleteRoles(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> ids = request.get("ids");
            roleService.deleteRoles(ids);
            return ResponseEntity.ok(ResponseData.success("角色批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除角色失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新角色状态
     * 
     * @param id 角色ID
     * @param request 状态信息
     * @return 响应结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseData<Object>> updateStatus(@PathVariable String id, 
                                                          @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            roleService.updateStatus(id, status);
            return ResponseEntity.ok(ResponseData.success("状态更新成功"));
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 配置角色权限
     * 
     * @param id 角色ID
     * @param request 资源ID列表
     * @return 响应结果
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<ResponseData<Object>> configurePermissions(@PathVariable String id, 
                                                                  @RequestBody Map<String, List<String>> request) {
        try {
            List<String> resourceIds = request.get("resourceIds");
            roleService.configurePermissions(id, resourceIds);
            return ResponseEntity.ok(ResponseData.success("权限配置成功"));
        } catch (Exception e) {
            log.error("权限配置失败", e);
            return ResponseEntity.ok(ResponseData.error("配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取角色权限列表
     * 
     * @param id 角色ID
     * @return 权限列表
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ResponseData<List<String>>> getRolePermissions(@PathVariable String id) {
        try {
            List<String> permissions = roleService.getRolePermissions(id);
            return ResponseEntity.ok(ResponseData.success(permissions));
        } catch (Exception e) {
            log.error("获取角色权限失败", e);
            return ResponseEntity.ok(ResponseData.error("获取失败: " + e.getMessage()));
        }
    }
}