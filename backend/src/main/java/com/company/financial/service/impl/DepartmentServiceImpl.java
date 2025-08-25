package com.company.financial.service.impl;

import com.company.financial.dto.department.*;
import com.company.financial.entity.Department;
import com.company.financial.entity.Position;
import com.company.financial.entity.User;
import com.company.financial.repository.DepartmentRepository;
import com.company.financial.repository.PositionRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.service.DepartmentService;
import com.company.financial.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<DepartmentTreeDTO> getDepartmentTree() {
        List<Department> departments = departmentRepository.findByDeletedOrderBySortOrderAsc(0);
        return buildTree(departments);
    }
    
    @Override
    public DepartmentDetailDTO getDepartmentById(String id) {
        Department department = departmentRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        return convertToDetailDTO(department);
    }
    
    @Override
    @Transactional
    public DepartmentDetailDTO createDepartment(CreateDepartmentDTO dto) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        if (!StringUtils.hasText(dto.getCode())) {
            dto.setCode(generateDepartmentCode());
        }
        
        if (departmentRepository.existsByCodeAndDeleted(dto.getCode(), 0)) {
            throw new BusinessException(400, "部门编码已存在");
        }
        
        if (checkDepartmentNameExists(dto.getName(), dto.getParentId(), null)) {
            throw new BusinessException(400, "同级部门名称已存在");
        }
        
        Department department = new Department();
        department.setId(UUID.randomUUID().toString());
        department.setCode(dto.getCode());
        department.setName(dto.getName());
        department.setType(dto.getType());
        department.setParentId(dto.getParentId());
        department.setManagerId(dto.getManagerId());
        department.setContactPhone(dto.getPhone());
        department.setDescription(dto.getDescription());
        department.setStatus(1);
        department.setSortOrder(0);
        department.setDeleted(0);
        department.setCreateTime(currentTime);
        department.setUpdateTime(currentTime);
        department.setCreateBy(currentUser);
        department.setUpdateBy(currentUser);
        
        if (StringUtils.hasText(dto.getParentId())) {
            Department parent = departmentRepository.findByIdAndDeleted(dto.getParentId(), 0)
                    .orElseThrow(() -> new BusinessException(404, "上级部门不存在"));
            department.setLevel(parent.getLevel() + 1);
            department.setPath(parent.getPath() + "/" + department.getId());
        } else {
            department.setLevel(1);
            department.setPath("/" + department.getId());
        }
        
        department = departmentRepository.save(department);
        
        return convertToDetailDTO(department);
    }
    
    @Override
    @Transactional
    public void updateDepartment(String id, UpdateDepartmentDTO dto) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Department department = departmentRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        if (checkDepartmentNameExists(dto.getName(), dto.getParentId(), id)) {
            throw new BusinessException(400, "同级部门名称已存在");
        }
        
        department.setName(dto.getName());
        department.setType(dto.getType());
        department.setManagerId(dto.getManagerId());
        department.setContactPhone(dto.getPhone());
        department.setDescription(dto.getDescription());
        department.setStatus("启用".equals(dto.getStatus()) ? 1 : 0);
        department.setUpdateTime(currentTime);
        department.setUpdateBy(currentUser);
        
        if (!Objects.equals(department.getParentId(), dto.getParentId())) {
            updateDepartmentHierarchy(department, dto.getParentId());
        }
        
        departmentRepository.save(department);
    }
    
    @Override
    @Transactional
    public void deleteDepartment(String id) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Department department = departmentRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        List<Department> children = departmentRepository.findByParentIdAndDeleted(id, 0);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "存在子部门，无法删除");
        }
        
        Integer employeeCount = userRepository.countByDepartmentIdAndDeleted(id, 0);
        if (employeeCount > 0) {
            throw new BusinessException(400, "部门下存在员工，无法删除");
        }
        
        department.setDeleted(1);
        department.setUpdateTime(currentTime);
        department.setUpdateBy(currentUser);
        
        departmentRepository.save(department);
    }
    
    @Override
    public DepartmentStatisticsDTO getDepartmentStatistics(String id) {
        Department department = departmentRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        DepartmentStatisticsDTO statistics = new DepartmentStatisticsDTO();
        statistics.setDepartmentId(id);
        statistics.setDepartmentName(department.getName());
        
        Integer totalEmployees = userRepository.countByDepartmentIdAndDeleted(id, 0);
        statistics.setTotalEmployees(totalEmployees);
        
        Integer positionCount = positionRepository.countByDepartmentId(id);
        statistics.setPositionCount(positionCount);
        
        Integer subDepartmentCount = departmentRepository.countByParentId(id);
        statistics.setSubDepartmentCount(subDepartmentCount);
        
        Double salaryBudget = positionRepository.sumSalaryBudgetByDepartmentId(id);
        statistics.setTotalSalaryBudget(BigDecimal.valueOf(salaryBudget != null ? salaryBudget : 0));
        
        Double actualSalary = positionRepository.sumActualSalaryByDepartmentId(id);
        statistics.setActualSalaryCost(BigDecimal.valueOf(actualSalary != null ? actualSalary : 0));
        
        return statistics;
    }
    
    @Override
    public String generateDepartmentCode() {
        String prefix = "DEPT";
        String maxCode = departmentRepository.findMaxCodeByPrefix(prefix);
        
        if (maxCode == null) {
            return prefix + "001";
        }
        
        String numberPart = maxCode.substring(prefix.length());
        int nextNumber = Integer.parseInt(numberPart) + 1;
        return String.format("%s%03d", prefix, nextNumber);
    }
    
    @Override
    public boolean checkDepartmentNameExists(String name, String parentId, String excludeId) {
        List<Department> departments = departmentRepository.findByParentIdAndDeleted(parentId, 0);
        
        return departments.stream()
                .anyMatch(d -> !d.getId().equals(excludeId) && d.getName().equals(name));
    }
    
    private List<DepartmentTreeDTO> buildTree(List<Department> departments) {
        Map<String, DepartmentTreeDTO> dtoMap = new HashMap<>();
        List<DepartmentTreeDTO> roots = new ArrayList<>();
        
        for (Department dept : departments) {
            DepartmentTreeDTO dto = convertToTreeDTO(dept);
            dtoMap.put(dept.getId(), dto);
        }
        
        for (Department dept : departments) {
            DepartmentTreeDTO dto = dtoMap.get(dept.getId());
            if (dept.getParentId() == null) {
                roots.add(dto);
            } else {
                DepartmentTreeDTO parent = dtoMap.get(dept.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        
        return roots;
    }
    
    private DepartmentTreeDTO convertToTreeDTO(Department department) {
        DepartmentTreeDTO dto = new DepartmentTreeDTO();
        dto.setId(department.getId());
        dto.setCode(department.getCode());
        dto.setName(department.getName());
        dto.setType(department.getType());
        dto.setParentId(department.getParentId());
        dto.setManagerId(department.getManagerId());
        dto.setPhone(department.getContactPhone());
        dto.setStatus(department.getStatus() == 1 ? "启用" : "停用");
        dto.setDescription(department.getDescription());
        
        if (department.getManagerId() != null) {
            userRepository.findById(department.getManagerId())
                    .ifPresent(user -> dto.setManager(user.getRealName()));
        }
        
        Integer employeeCount = userRepository.countByDepartmentIdAndDeleted(department.getId(), 0);
        dto.setEmployeeCount(employeeCount);
        
        return dto;
    }
    
    private DepartmentDetailDTO convertToDetailDTO(Department department) {
        DepartmentDetailDTO dto = new DepartmentDetailDTO();
        dto.setId(department.getId());
        dto.setCode(department.getCode());
        dto.setName(department.getName());
        dto.setType(department.getType());
        dto.setParentId(department.getParentId());
        dto.setManagerId(department.getManagerId());
        dto.setPhone(department.getContactPhone());
        dto.setStatus(department.getStatus() == 1 ? "启用" : "停用");
        dto.setDescription(department.getDescription());
        dto.setLevel(department.getLevel());
        dto.setPath(department.getPath());
        dto.setCreateTime(department.getCreateTime());
        dto.setUpdateTime(department.getUpdateTime());
        dto.setCreateBy(department.getCreateBy());
        dto.setUpdateBy(department.getUpdateBy());
        
        if (department.getParentId() != null) {
            departmentRepository.findByIdAndDeleted(department.getParentId(), 0)
                    .ifPresent(parent -> dto.setParentName(parent.getName()));
        }
        
        if (department.getManagerId() != null) {
            userRepository.findById(department.getManagerId())
                    .ifPresent(user -> dto.setManager(user.getRealName()));
        }
        
        Integer employeeCount = userRepository.countByDepartmentIdAndDeleted(department.getId(), 0);
        dto.setEmployeeCount(employeeCount);
        
        return dto;
    }
    
    private String getDepartmentType(Integer level) {
        if (level == 1) return "公司";
        if (level == 2) return "部门";
        return "小组";
    }
    
    private void updateDepartmentHierarchy(Department department, String newParentId) {
        if (newParentId != null) {
            Department newParent = departmentRepository.findByIdAndDeleted(newParentId, 0)
                    .orElseThrow(() -> new BusinessException(404, "上级部门不存在"));
            department.setLevel(newParent.getLevel() + 1);
            department.setPath(newParent.getPath() + "/" + department.getId());
        } else {
            department.setLevel(1);
            department.setPath("/" + department.getId());
        }
        
        department.setParentId(newParentId);
        
        List<Department> children = departmentRepository.findByParentIdAndDeleted(department.getId(), 0);
        for (Department child : children) {
            updateDepartmentHierarchy(child, department.getId());
            departmentRepository.save(child);
        }
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
    
    @Override
    public List<EmployeeDTO> getDepartmentEmployees(String departmentId) {
        Department department = departmentRepository.findByIdAndDeleted(departmentId, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        List<User> users = userRepository.findByDepartmentIdAndDeleted(departmentId, 0);
        return users.stream().map(this::convertToEmployeeDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void assignEmployeeToDepartment(String departmentId, String userId, String positionId) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        Department department = departmentRepository.findByIdAndDeleted(departmentId, 0)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        
        User user = userRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        if (user.getDepartmentId() != null && !user.getDepartmentId().equals(departmentId)) {
            throw new BusinessException(400, "该员工已分配到其他部门");
        }
        
        if (positionId != null) {
            Position position = positionRepository.findByIdAndDeleted(positionId, 0)
                    .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
            
            if (!position.getDepartmentId().equals(departmentId)) {
                throw new BusinessException(400, "岗位不属于该部门");
            }
            
            user.setPositionId(positionId);
            user.setPosition(position.getName());
        }
        
        user.setDepartmentId(departmentId);
        user.setDepartment(department.getName());
        user.setUpdateTime(currentTime);
        user.setUpdateBy(currentUser);
        
        userRepository.save(user);
        
        // 更新岗位的实际人数
        if (positionId != null) {
            updatePositionActualCount(positionId);
        }
    }
    
    @Override
    @Transactional
    public void removeEmployeeFromDepartment(String departmentId, String userId) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        User user = userRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        if (user.getDepartmentId() == null || !user.getDepartmentId().equals(departmentId)) {
            throw new BusinessException(400, "该员工不属于此部门");
        }
        
        // 保存原岗位ID用于更新实际人数
        String oldPositionId = user.getPositionId();
        
        user.setDepartmentId(null);
        user.setDepartment(null);
        user.setPositionId(null);
        user.setPosition(null);
        user.setUpdateTime(currentTime);
        user.setUpdateBy(currentUser);
        
        userRepository.save(user);
        
        // 更新原岗位的实际人数
        if (oldPositionId != null) {
            updatePositionActualCount(oldPositionId);
        }
    }
    
    @Override
    @Transactional
    public void transferEmployee(TransferEmployeeDTO dto) {
        String currentUser = getCurrentUser();
        Long currentTime = System.currentTimeMillis();
        
        User user = userRepository.findByIdAndDeleted(dto.getUserId(), 0)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        if (!dto.getFromDepartmentId().equals(user.getDepartmentId())) {
            throw new BusinessException(400, "员工不属于原部门");
        }
        
        Department toDepartment = departmentRepository.findByIdAndDeleted(dto.getToDepartmentId(), 0)
                .orElseThrow(() -> new BusinessException(404, "目标部门不存在"));
        
        // 保存原岗位ID用于更新实际人数
        String oldPositionId = user.getPositionId();
        
        user.setDepartmentId(dto.getToDepartmentId());
        user.setDepartment(toDepartment.getName());
        
        if (dto.getPositionId() != null) {
            Position position = positionRepository.findByIdAndDeleted(dto.getPositionId(), 0)
                    .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
            
            if (!position.getDepartmentId().equals(dto.getToDepartmentId())) {
                throw new BusinessException(400, "岗位不属于目标部门");
            }
            
            user.setPositionId(dto.getPositionId());
            user.setPosition(position.getName());
        } else {
            user.setPositionId(null);
            user.setPosition(null);
        }
        
        user.setUpdateTime(currentTime);
        user.setUpdateBy(currentUser);
        
        userRepository.save(user);
        
        // 更新原岗位和新岗位的实际人数
        if (oldPositionId != null) {
            updatePositionActualCount(oldPositionId);
        }
        if (dto.getPositionId() != null && !dto.getPositionId().equals(oldPositionId)) {
            updatePositionActualCount(dto.getPositionId());
        }
    }
    
    @Override
    public List<EmployeeDTO> getAvailableEmployees() {
        List<User> users = userRepository.findAvailableEmployees();
        return users.stream().map(this::convertToEmployeeDTO).collect(Collectors.toList());
    }
    
    private EmployeeDTO convertToEmployeeDTO(User user) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmployeeId(user.getUsername()); // 暂时使用username作为工号
        dto.setDepartmentId(user.getDepartmentId());
        dto.setDepartmentName(user.getDepartment());
        dto.setPositionId(user.getPositionId());
        dto.setPositionName(user.getPosition());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setHireDate(user.getCreateTime()); // 暂时使用创建时间作为入职时间
        dto.setStatus(user.getStatus());
        return dto;
    }
    
    private void updatePositionActualCount(String positionId) {
        if (positionId == null) {
            return;
        }
        
        try {
            // 统计该岗位的实际人数
            Integer actualCount = userRepository.countByPositionIdAndDeleted(positionId, 0);
            
            // 更新岗位的实际人数
            Position position = positionRepository.findByIdAndDeleted(positionId, 0).orElse(null);
            if (position != null) {
                position.setActualCount(actualCount);
                position.setUpdateTime(System.currentTimeMillis());
                position.setUpdateBy(getCurrentUser());
                positionRepository.save(position);
            }
        } catch (Exception e) {
            log.error("更新岗位实际人数失败，岗位ID: {}", positionId, e);
        }
    }
}