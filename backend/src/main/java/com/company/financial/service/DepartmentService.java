package com.company.financial.service;

import com.company.financial.dto.department.*;
import java.util.List;

public interface DepartmentService {
    
    List<DepartmentTreeDTO> getDepartmentTree();
    
    DepartmentDetailDTO getDepartmentById(String id);
    
    DepartmentDetailDTO createDepartment(CreateDepartmentDTO dto);
    
    void updateDepartment(String id, UpdateDepartmentDTO dto);
    
    void deleteDepartment(String id);
    
    DepartmentStatisticsDTO getDepartmentStatistics(String id);
    
    String generateDepartmentCode();
    
    boolean checkDepartmentNameExists(String name, String parentId, String excludeId);
    
    List<EmployeeDTO> getDepartmentEmployees(String departmentId);
    
    void assignEmployeeToDepartment(String departmentId, String userId, String positionId);
    
    void removeEmployeeFromDepartment(String departmentId, String userId);
    
    void transferEmployee(TransferEmployeeDTO dto);
    
    List<EmployeeDTO> getAvailableEmployees();
}