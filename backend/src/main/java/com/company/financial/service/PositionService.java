package com.company.financial.service;

import com.company.financial.dto.position.*;
import java.util.List;

public interface PositionService {
    
    List<PositionDTO> getPositionsByDepartmentId(String departmentId);
    
    PositionDTO getPositionById(String id);
    
    PositionDTO createPosition(CreatePositionDTO dto);
    
    void updatePosition(String id, UpdatePositionDTO dto);
    
    void deletePosition(String id);
    
    void updateActualCount(String positionId, Integer actualCount);
    
    boolean checkPositionNameExists(String name, String departmentId, String excludeId);
    
    void syncAllPositionActualCount();
}