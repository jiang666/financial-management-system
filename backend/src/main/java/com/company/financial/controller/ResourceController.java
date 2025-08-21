package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.entity.Resource;
import com.company.financial.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 * 
 * @author System
 */
@RestController
@RequestMapping("/v1/resources")
@Slf4j
public class ResourceController {
    
    @Autowired
    private ResourceService resourceService;
    
    /**
     * 获取资源树形结构
     * 
     * @return 资源树
     */
    @GetMapping("/tree")
    public ResponseEntity<ResponseData<List<Resource>>> getResourceTree() {
        try {
            List<Resource> tree = resourceService.getResourceTree();
            return ResponseEntity.ok(ResponseData.success(tree));
        } catch (Exception e) {
            log.error("获取资源树失败", e);
            return ResponseEntity.ok(ResponseData.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据用户ID获取用户菜单
     * 
     * @param userId 用户ID
     * @return 用户菜单
     */
    @GetMapping("/user/{userId}/menus")
    public ResponseEntity<ResponseData<List<Resource>>> getUserMenus(@PathVariable String userId) {
        try {
            List<Resource> menus = resourceService.getUserMenus(userId);
            return ResponseEntity.ok(ResponseData.success(menus));
        } catch (Exception e) {
            log.error("获取用户菜单失败", e);
            return ResponseEntity.ok(ResponseData.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据用户ID获取用户权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/user/{userId}/permissions")
    public ResponseEntity<ResponseData<List<String>>> getUserPermissions(@PathVariable String userId) {
        try {
            List<String> permissions = resourceService.getUserPermissions(userId);
            return ResponseEntity.ok(ResponseData.success(permissions));
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return ResponseEntity.ok(ResponseData.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查用户是否有指定权限
     * 
     * @param userId 用户ID
     * @param permission 权限编码
     * @return 是否有权限
     */
    @GetMapping("/user/{userId}/permission/{permission}")
    public ResponseEntity<ResponseData<Boolean>> hasPermission(@PathVariable String userId, 
                                                              @PathVariable String permission) {
        try {
            boolean hasPermission = resourceService.hasPermission(userId, permission);
            return ResponseEntity.ok(ResponseData.success(hasPermission));
        } catch (Exception e) {
            log.error("检查用户权限失败", e);
            return ResponseEntity.ok(ResponseData.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据资源类型获取资源列表
     * 
     * @param type 资源类型
     * @return 资源列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ResponseData<List<Resource>>> getResourcesByType(@PathVariable String type) {
        try {
            List<Resource> resources = resourceService.getResourcesByType(type);
            return ResponseEntity.ok(ResponseData.success(resources));
        } catch (Exception e) {
            log.error("根据类型获取资源失败", e);
            return ResponseEntity.ok(ResponseData.error("获取失败: " + e.getMessage()));
        }
    }
}