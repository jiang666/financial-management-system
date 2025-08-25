package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.dto.position.*;
import com.company.financial.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 岗位管理控制器
 * 
 * @author System
 */

@RestController
@RequestMapping("/v1/positions")
@Slf4j
public class PositionController {
    
    @Autowired
    private PositionService positionService;
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<PositionDTO>> getPositionById(@PathVariable String id) {
        try {
            PositionDTO position = positionService.getPositionById(id);
            return ResponseEntity.ok(ResponseData.success(position));
        } catch (Exception e) {
            log.error("查询岗位详情失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ResponseData<PositionDTO>> createPosition(@Valid @RequestBody CreatePositionDTO dto) {
        try {
            PositionDTO position = positionService.createPosition(dto);
            return ResponseEntity.ok(ResponseData.success("创建成功", position));
        } catch (Exception e) {
            log.error("创建岗位失败", e);
            return ResponseEntity.ok(ResponseData.error("创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> updatePosition(
            @PathVariable String id,
            @Valid @RequestBody UpdatePositionDTO dto) {
        try {
            positionService.updatePosition(id, dto);
            return ResponseEntity.ok(ResponseData.success("更新成功", null));
        } catch (Exception e) {
            log.error("更新岗位失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deletePosition(@PathVariable String id) {
        try {
            positionService.deletePosition(id);
            return ResponseEntity.ok(ResponseData.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除岗位失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/sync-actual-count")
    public ResponseEntity<ResponseData<String>> syncAllPositionActualCount() {
        try {
            positionService.syncAllPositionActualCount();
            return ResponseEntity.ok(ResponseData.success("同步成功"));
        } catch (Exception e) {
            log.error("同步岗位实际人数失败", e);
            return ResponseEntity.ok(ResponseData.error("同步失败: " + e.getMessage()));
        }
    }
}