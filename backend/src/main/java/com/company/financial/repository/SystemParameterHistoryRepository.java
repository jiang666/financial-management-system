package com.company.financial.repository;

import com.company.financial.entity.SystemParameterHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 系统参数历史记录Repository接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Repository
public interface SystemParameterHistoryRepository extends JpaRepository<SystemParameterHistory, String> {
    
    /**
     * 根据参数键和时间范围查询历史记录
     * 
     * @param paramKey 参数键
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT h FROM SystemParameterHistory h WHERE " +
           "h.paramKey = :paramKey AND " +
           "(:startTime IS NULL OR h.operateTime >= :startTime) AND " +
           "(:endTime IS NULL OR h.operateTime <= :endTime) " +
           "ORDER BY h.operateTime DESC")
    Page<SystemParameterHistory> findByParamKeyAndTimeRange(@Param("paramKey") String paramKey,
                                                            @Param("startTime") Long startTime,
                                                            @Param("endTime") Long endTime,
                                                            Pageable pageable);
}