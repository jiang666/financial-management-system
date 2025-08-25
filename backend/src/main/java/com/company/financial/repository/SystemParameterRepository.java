package com.company.financial.repository;

import com.company.financial.entity.SystemParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统参数Repository接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, String> {
    
    /**
     * 根据参数键查找参数
     * 
     * @param paramKey 参数键
     * @return 参数对象
     */
    Optional<SystemParameter> findByParamKey(String paramKey);
    
    /**
     * 根据参数键和删除标记查找参数
     * 
     * @param paramKey 参数键
     * @param deleted 删除标记
     * @return 参数对象
     */
    Optional<SystemParameter> findByParamKeyAndDeleted(String paramKey, Integer deleted);
    
    /**
     * 根据分类查找参数列表
     * 
     * @param category 参数分类
     * @param deleted 删除标记
     * @return 参数列表
     */
    List<SystemParameter> findByCategoryAndDeletedOrderBySortOrderAsc(String category, Integer deleted);
    
    /**
     * 分页查询参数列表
     * 
     * @param category 参数分类（可选）
     * @param keyword 关键字（可选）
     * @param isSystem 是否系统参数（可选）
     * @param visible 是否可见（可选）
     * @param deleted 删除标记
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT sp FROM SystemParameter sp WHERE " +
           "(:category IS NULL OR sp.category = :category) AND " +
           "(:keyword IS NULL OR sp.paramKey LIKE %:keyword% OR sp.description LIKE %:keyword%) AND " +
           "(:isSystem IS NULL OR sp.isSystem = :isSystem) AND " +
           "(:visible IS NULL OR sp.visible = :visible) AND " +
           "sp.deleted = :deleted " +
           "ORDER BY sp.category, sp.sortOrder")
    Page<SystemParameter> findByConditions(@Param("category") String category,
                                          @Param("keyword") String keyword,
                                          @Param("isSystem") Boolean isSystem,
                                          @Param("visible") Boolean visible,
                                          @Param("deleted") Integer deleted,
                                          Pageable pageable);
    
    /**
     * 根据参数键列表批量查找参数
     * 
     * @param paramKeys 参数键列表
     * @param deleted 删除标记
     * @return 参数列表
     */
    List<SystemParameter> findByParamKeyInAndDeleted(List<String> paramKeys, Integer deleted);
    
    /**
     * 查找所有可见的参数
     * 
     * @param visible 是否可见
     * @param deleted 删除标记
     * @return 参数列表
     */
    List<SystemParameter> findByVisibleAndDeletedOrderByCategoryAscSortOrderAsc(Boolean visible, Integer deleted);
    
    /**
     * 根据分类统计参数数量
     * 
     * @param category 参数分类
     * @param deleted 删除标记
     * @return 参数数量
     */
    Long countByCategoryAndDeleted(String category, Integer deleted);
    
    /**
     * 检查参数键是否存在（排除指定ID）
     * 
     * @param paramKey 参数键
     * @param id 排除的ID
     * @param deleted 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(sp) > 0 FROM SystemParameter sp WHERE " +
           "sp.paramKey = :paramKey AND sp.id != :id AND sp.deleted = :deleted")
    boolean existsByParamKeyExcludeId(@Param("paramKey") String paramKey, 
                                      @Param("id") String id, 
                                      @Param("deleted") Integer deleted);
}