package com.company.financial.service.impl;

import com.company.financial.dto.position.*;
import com.company.financial.entity.Department;
import com.company.financial.entity.Position;
import com.company.financial.repository.DepartmentRepository;
import com.company.financial.repository.PositionRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.service.PositionService;
import com.company.financial.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<PositionDTO> getPositionsByDepartmentId(String departmentId) {
        Department department = departmentRepository.findByIdAndDeleted(departmentId, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        List<Position> positions = positionRepository.findByDepartmentIdAndDeleted(departmentId, 0);
        
        return positions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PositionDTO getPositionById(String id) {
        Position position = positionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        
        return convertToDTO(position);
    }
    
    @Override
    @Transactional
    public PositionDTO createPosition(CreatePositionDTO dto) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Department department = departmentRepository.findByIdAndDeleted(dto.getDepartmentId(), 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        if (positionRepository.existsByNameAndDepartmentIdAndDeleted(dto.getName(), dto.getDepartmentId(), 0)) {
            throw new BusinessException(400, "该部门下岗位名称已存在");
        }
        
        Position position = new Position();
        position.setId(UUID.randomUUID().toString());
        position.setDepartmentId(dto.getDepartmentId());
        position.setName(dto.getName());
        position.setLevel(dto.getLevel());
        position.setCount(dto.getCount());
        position.setActualCount(0);
        position.setSalary(dto.getSalary());
        position.setStatus(1);
        position.setDescription(dto.getDescription());
        position.setDeleted(0);
        position.setCreateTime(currentTime);
        position.setUpdateTime(currentTime);
        position.setCreateBy(currentUser);
        position.setUpdateBy(currentUser);
        
        position = positionRepository.save(position);
        
        return convertToDTO(position);
    }
    
    @Override
    @Transactional
    public void updatePosition(String id, UpdatePositionDTO dto) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Position position = positionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        
        if (checkPositionNameExists(dto.getName(), position.getDepartmentId(), id)) {
            throw new BusinessException(400, "该部门下岗位名称已存在");
        }
        
        position.setName(dto.getName());
        position.setLevel(dto.getLevel());
        position.setCount(dto.getCount());
        position.setSalary(dto.getSalary());
        position.setStatus("启用".equals(dto.getStatus()) ? 1 : 0);
        position.setDescription(dto.getDescription());
        position.setUpdateTime(currentTime);
        position.setUpdateBy(currentUser);
        
        positionRepository.save(position);
    }
    
    @Override
    @Transactional
    public void deletePosition(String id) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Position position = positionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        
        if (position.getActualCount() > 0) {
            throw new BusinessException(400, "该岗位下存在员工，无法删除");
        }
        
        position.setDeleted(1);
        position.setUpdateTime(currentTime);
        position.setUpdateBy(currentUser);
        
        positionRepository.save(position);
    }
    
    @Override
    @Transactional
    public void updateActualCount(String positionId, Integer actualCount) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Position position = positionRepository.findByIdAndDeleted(positionId, 0)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        
        if (actualCount > position.getCount()) {
            throw new BusinessException(400, "实际人数不能超过编制人数");
        }
        
        position.setActualCount(actualCount);
        position.setUpdateTime(currentTime);
        position.setUpdateBy(currentUser);
        
        positionRepository.save(position);
    }
    
    @Override
    public boolean checkPositionNameExists(String name, String departmentId, String excludeId) {
        List<Position> positions = positionRepository.findByDepartmentIdAndDeleted(departmentId, 0);
        
        return positions.stream()
                .anyMatch(p -> !p.getId().equals(excludeId) && p.getName().equals(name));
    }
    
    private PositionDTO convertToDTO(Position position) {
        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setDepartmentId(position.getDepartmentId());
        dto.setName(position.getName());
        dto.setLevel(position.getLevel());
        dto.setCount(position.getCount());
        
        // 动态计算实际人数，确保数据准确
        Integer actualCount = userRepository.countByPositionIdAndDeleted(position.getId(), 0);
        dto.setActualCount(actualCount);
        
        dto.setSalary(position.getSalary());
        dto.setStatus(position.getStatus() == 1 ? "启用" : "停用");
        dto.setDescription(position.getDescription());
        dto.setCreateTime(position.getCreateTime());
        dto.setUpdateTime(position.getUpdateTime());
        
        return dto;
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
    
    @Override
    @Transactional
    public void syncAllPositionActualCount() {
        log.info("开始同步所有岗位的实际人数");
        
        List<Position> positions = positionRepository.findByDeleted(0);
        
        for (Position position : positions) {
            try {
                Integer actualCount = userRepository.countByPositionIdAndDeleted(position.getId(), 0);
                position.setActualCount(actualCount);
                position.setUpdateTime(System.currentTimeMillis());
                position.setUpdateBy(getCurrentUser());
                positionRepository.save(position);
                
                log.info("岗位 {} 实际人数更新为: {}", position.getName(), actualCount);
            } catch (Exception e) {
                log.error("更新岗位 {} 实际人数失败", position.getName(), e);
            }
        }
        
        log.info("岗位实际人数同步完成");
    }
}