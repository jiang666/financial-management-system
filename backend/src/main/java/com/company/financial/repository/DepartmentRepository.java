package com.company.financial.repository;

import com.company.financial.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    
    List<Department> findByDeletedOrderBySortOrderAsc(Integer deleted);
    
    List<Department> findByParentIdAndDeleted(String parentId, Integer deleted);
    
    Optional<Department> findByIdAndDeleted(String id, Integer deleted);
    
    Optional<Department> findByCodeAndDeleted(String code, Integer deleted);
    
    boolean existsByCodeAndDeleted(String code, Integer deleted);
    
    boolean existsByNameAndParentIdAndDeleted(String name, String parentId, Integer deleted);
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.parentId = :parentId AND d.deleted = 0")
    Integer countByParentId(@Param("parentId") String parentId);
    
    @Query("SELECT MAX(d.code) FROM Department d WHERE d.code LIKE :prefix%")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);
    
    @Query("SELECT d FROM Department d WHERE d.managerId = :managerId AND d.deleted = 0")
    List<Department> findByManagerId(@Param("managerId") String managerId);
}