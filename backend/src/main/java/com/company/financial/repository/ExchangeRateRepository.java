package com.company.financial.repository;

import com.company.financial.entity.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 汇率Repository接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
    
    /**
     * 获取最新的汇率记录
     */
    @Query(value = "SELECT er.* FROM exchange_rates er WHERE er.deleted = 0 " +
           "AND er.from_currency_id = :fromCurrencyId " +
           "AND er.to_currency_id = :toCurrencyId " +
           "AND er.status = 1 " +
           "AND er.effective_date <= :currentTime " +
           "AND (er.expiry_date IS NULL OR er.expiry_date > :currentTime) " +
           "ORDER BY er.effective_date DESC LIMIT 1", 
           nativeQuery = true)
    Optional<ExchangeRate> findLatestRate(@Param("fromCurrencyId") String fromCurrencyId,
                                         @Param("toCurrencyId") String toCurrencyId,
                                         @Param("currentTime") Long currentTime);
    
    /**
     * 根据币种ID获取所有相关汇率
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.deleted = 0 " +
           "AND (er.fromCurrencyId = :currencyId OR er.toCurrencyId = :currencyId) " +
           "AND er.status = 1")
    List<ExchangeRate> findByCurrencyId(@Param("currencyId") String currencyId);
    
    /**
     * 条件查询汇率历史 - 暂时移除currencyCode的查询条件
     */
    @Query("SELECT er FROM ExchangeRate er " +
           "WHERE er.deleted = 0 " +
           "AND (:currencyId IS NULL OR er.fromCurrencyId = :currencyId OR er.toCurrencyId = :currencyId) " +
           "AND (:startDate IS NULL OR er.effectiveDate >= :startDate) " +
           "AND (:endDate IS NULL OR er.effectiveDate <= :endDate) " +
           "AND (:source IS NULL OR er.source = :source)")
    Page<ExchangeRate> findRateHistory(@Param("currencyId") String currencyId,
                                      @Param("startDate") Long startDate,
                                      @Param("endDate") Long endDate,
                                      @Param("source") String source,
                                      Pageable pageable);
    
    /**
     * 获取指定币种对的所有汇率历史
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.deleted = 0 " +
           "AND er.fromCurrencyId = :fromCurrencyId " +
           "AND er.toCurrencyId = :toCurrencyId " +
           "ORDER BY er.effectiveDate DESC")
    List<ExchangeRate> findRateHistoryByCurrencyPair(@Param("fromCurrencyId") String fromCurrencyId,
                                                     @Param("toCurrencyId") String toCurrencyId);
    
    /**
     * 删除指定币种的所有汇率记录
     */
    @Modifying
    @Query("UPDATE ExchangeRate er SET er.deleted = 1, er.updatedAt = :updateTime, er.updatedBy = :updatedBy " +
           "WHERE er.deleted = 0 AND (er.fromCurrencyId = :currencyId OR er.toCurrencyId = :currencyId)")
    void logicalDeleteByCurrencyId(@Param("currencyId") String currencyId,
                                  @Param("updateTime") Long updateTime,
                                  @Param("updatedBy") String updatedBy);
}