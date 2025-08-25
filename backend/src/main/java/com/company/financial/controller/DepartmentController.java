package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.dto.department.*;
import com.company.financial.dto.position.PositionDTO;
import com.company.financial.service.DepartmentService;
import com.company.financial.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 部门管理控制器
 * 
 * @author System
 */

@RestController
@RequestMapping("/v1/departments")
@Slf4j
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private PositionService positionService;
    
    @GetMapping("/tree")
    public ResponseEntity<ResponseData<List<DepartmentTreeDTO>>> getDepartmentTree() {
        try {
            List<DepartmentTreeDTO> tree = departmentService.getDepartmentTree();
            return ResponseEntity.ok(ResponseData.success(tree));
        } catch (Exception e) {
            log.error("查询部门树失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<DepartmentDetailDTO>> getDepartmentById(@PathVariable String id) {
        try {
            DepartmentDetailDTO department = departmentService.getDepartmentById(id);
            return ResponseEntity.ok(ResponseData.success(department));
        } catch (Exception e) {
            log.error("查询部门详情失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ResponseData<DepartmentDetailDTO>> createDepartment(@Valid @RequestBody CreateDepartmentDTO dto) {
        try {
            DepartmentDetailDTO department = departmentService.createDepartment(dto);
            return ResponseEntity.ok(ResponseData.success("创建成功", department));
        } catch (Exception e) {
            log.error("创建部门失败", e);
            return ResponseEntity.ok(ResponseData.error("创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody UpdateDepartmentDTO dto) {
        try {
            departmentService.updateDepartment(id, dto);
            return ResponseEntity.ok(ResponseData.success("更新成功", null));
        } catch (Exception e) {
            log.error("更新部门失败", e);
            return ResponseEntity.ok(ResponseData.error("更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteDepartment(@PathVariable String id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ResponseData.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除部门失败", e);
            return ResponseEntity.ok(ResponseData.error("删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ResponseData<DepartmentStatisticsDTO>> getDepartmentStatistics(@PathVariable String id) {
        try {
            DepartmentStatisticsDTO statistics = departmentService.getDepartmentStatistics(id);
            return ResponseEntity.ok(ResponseData.success(statistics));
        } catch (Exception e) {
            log.error("查询部门统计信息失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{departmentId}/positions")
    public ResponseEntity<ResponseData<List<PositionDTO>>> getDepartmentPositions(@PathVariable String departmentId) {
        try {
            List<PositionDTO> positions = positionService.getPositionsByDepartmentId(departmentId);
            return ResponseEntity.ok(ResponseData.success(positions));
        } catch (Exception e) {
            log.error("查询部门岗位列表失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<ResponseData<List<EmployeeDTO>>> getDepartmentEmployees(@PathVariable String departmentId) {
        try {
            List<EmployeeDTO> employees = departmentService.getDepartmentEmployees(departmentId);
            return ResponseEntity.ok(ResponseData.success(employees));
        } catch (Exception e) {
            log.error("查询部门员工列表失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{departmentId}/employees")
    public ResponseEntity<ResponseData<String>> assignEmployee(
            @PathVariable String departmentId,
            @Valid @RequestBody AssignEmployeeDTO dto) {
        try {
            departmentService.assignEmployeeToDepartment(departmentId, dto.getUserId(), dto.getPositionId());
            return ResponseEntity.ok(ResponseData.success("分配成功"));
        } catch (Exception e) {
            log.error("分配员工失败", e);
            return ResponseEntity.ok(ResponseData.error("分配失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{departmentId}/employees/{userId}")
    public ResponseEntity<ResponseData<String>> removeEmployee(
            @PathVariable String departmentId,
            @PathVariable String userId) {
        try {
            departmentService.removeEmployeeFromDepartment(departmentId, userId);
            return ResponseEntity.ok(ResponseData.success("移除成功"));
        } catch (Exception e) {
            log.error("移除员工失败", e);
            return ResponseEntity.ok(ResponseData.error("移除失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/employees/transfer")
    public ResponseEntity<ResponseData<String>> transferEmployee(@Valid @RequestBody TransferEmployeeDTO dto) {
        try {
            departmentService.transferEmployee(dto);
            return ResponseEntity.ok(ResponseData.success("调动成功"));
        } catch (Exception e) {
            log.error("调动员工失败", e);
            return ResponseEntity.ok(ResponseData.error("调动失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/employees/available")
    public ResponseEntity<ResponseData<List<EmployeeDTO>>> getAvailableEmployees() {
        try {
            List<EmployeeDTO> employees = departmentService.getAvailableEmployees();
            return ResponseEntity.ok(ResponseData.success(employees));
        } catch (Exception e) {
            log.error("查询可用员工列表失败", e);
            return ResponseEntity.ok(ResponseData.error("查询失败: " + e.getMessage()));
        }
    }
}