package com.company.financial.repository;

import com.company.financial.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    
    List<Position> findByDepartmentIdAndDeleted(String departmentId, Integer deleted);
    
    Optional<Position> findByIdAndDeleted(String id, Integer deleted);
    
    boolean existsByNameAndDepartmentIdAndDeleted(String name, String departmentId, Integer deleted);
    
    @Query("SELECT COUNT(p) FROM Position p WHERE p.departmentId = :departmentId AND p.deleted = 0")
    Integer countByDepartmentId(@Param("departmentId") String departmentId);
    
    @Query("SELECT SUM(p.count) FROM Position p WHERE p.departmentId = :departmentId AND p.deleted = 0")
    Integer sumCountByDepartmentId(@Param("departmentId") String departmentId);
    
    @Query("SELECT SUM(p.actualCount) FROM Position p WHERE p.departmentId = :departmentId AND p.deleted = 0")
    Integer sumActualCountByDepartmentId(@Param("departmentId") String departmentId);
    
    @Query("SELECT SUM(p.salary * p.count) FROM Position p WHERE p.departmentId = :departmentId AND p.deleted = 0")
    Double sumSalaryBudgetByDepartmentId(@Param("departmentId") String departmentId);
    
    @Query("SELECT SUM(p.salary * p.actualCount) FROM Position p WHERE p.departmentId = :departmentId AND p.deleted = 0")
    Double sumActualSalaryByDepartmentId(@Param("departmentId") String departmentId);
    
    List<Position> findByDeleted(Integer deleted);
}