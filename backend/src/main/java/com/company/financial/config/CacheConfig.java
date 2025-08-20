package com.company.financial.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        // 使用内存缓存，生产环境可以考虑使用Redis
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 设置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "userPermissions",      // 用户权限缓存
                "userPermissionCheck",  // 用户权限检查缓存
                "userRoleCheck"         // 用户角色检查缓存
        ));
        
        return cacheManager;
    }
}