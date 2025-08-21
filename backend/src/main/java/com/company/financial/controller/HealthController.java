package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author System
 */
@RestController
@RequestMapping("/v1/health")
public class HealthController {
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/check")
    public ResponseEntity<ResponseData<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "financial-management-system");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(ResponseData.success("服务运行正常", health));
    }
}