package com.company.financial.service.impl;

import com.company.financial.entity.Resource;
import com.company.financial.repository.ResourceRepository;
import com.company.financial.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源服务实现类
 * 
 * @author System
 */
@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Override
    public List<Resource> getResourceTree() {
        return resourceRepository.findByDeletedOrderByLevelAscSortOrderAsc(0);
    }
    
    @Override
    public List<Resource> getUserMenus(String userId) {
        List<Resource> allResources = resourceRepository.findResourcesByUserId(userId);
        
        // 只返回菜单类型的资源
        return allResources.stream()
                .filter(resource -> "TOP_MENU".equals(resource.getResourceType()) || 
                                  "SUB_MENU".equals(resource.getResourceType()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getUserPermissions(String userId) {
        List<Resource> allResources = resourceRepository.findResourcesByUserId(userId);
        
        return allResources.stream()
                .map(Resource::getResourceCode)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasPermission(String userId, String permission) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }
    
    @Override
    public List<Resource> getResourcesByType(String resourceType) {
        return resourceRepository.findByResourceTypeAndDeletedOrderBySortOrderAsc(resourceType, 0);
    }
}