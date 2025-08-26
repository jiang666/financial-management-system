package com.company.financial.repository;

import com.company.financial.entity.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 币种Repository接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    
    /**
     * 查找本位币
     */
    @Query("SELECT c FROM Currency c WHERE c.deleted = 0 AND c.isBase = 1")
    Optional<Currency> findBaseCurrency();
    
    /**
     * 根据币种代码查找
     */
    @Query("SELECT c FROM Currency c WHERE c.deleted = 0 AND c.code = :code")
    Optional<Currency> findByCode(@Param("code") String code);
    
    /**
     * 条件查询币种列表
     */
    @Query("SELECT c FROM Currency c WHERE c.deleted = 0 " +
           "AND (:code IS NULL OR c.code LIKE %:code%) " +
           "AND (:name IS NULL OR c.name LIKE %:name%) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:isBase IS NULL OR c.isBase = :isBase)")
    Page<Currency> findByConditions(@Param("code") String code,
                                   @Param("name") String name,
                                   @Param("status") Integer status,
                                   @Param("isBase") Integer isBase,
                                   Pageable pageable);
    
    /**
     * 查找所有启用的币种
     */
    @Query("SELECT c FROM Currency c WHERE c.deleted = 0 AND c.status = 1")
    List<Currency> findAllActiveCurrencies();
    
    /**
     * 根据ID列表查找币种
     */
    @Query("SELECT c FROM Currency c WHERE c.deleted = 0 AND c.id IN :ids")
    List<Currency> findByIds(@Param("ids") List<String> ids);
    
    /**
     * 检查币种代码是否已存在
     */
    @Query("SELECT COUNT(c) > 0 FROM Currency c WHERE c.deleted = 0 AND c.code = :code AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsByCode(@Param("code") String code, @Param("excludeId") String excludeId);
    
    /**
     * 统计本位币数量
     */
    @Query("SELECT COUNT(c) FROM Currency c WHERE c.deleted = 0 AND c.isBase = 1")
    long countBaseCurrencies();
}