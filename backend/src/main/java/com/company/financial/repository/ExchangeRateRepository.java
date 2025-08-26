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
    @Query("SELECT er FROM ExchangeRate er WHERE er.deleted = 0 " +
           "AND er.fromCurrencyId = :fromCurrencyId " +
           "AND er.toCurrencyId = :toCurrencyId " +
           "AND er.status = 1 " +
           "AND er.effectiveDate <= :currentTime " +
           "AND (er.expiryDate IS NULL OR er.expiryDate > :currentTime) " +
           "ORDER BY er.effectiveDate DESC")
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
     * 条件查询汇率历史
     */
    @Query("SELECT er FROM ExchangeRate er " +
           "LEFT JOIN Currency fc ON er.fromCurrencyId = fc.id " +
           "LEFT JOIN Currency tc ON er.toCurrencyId = tc.id " +
           "WHERE er.deleted = 0 " +
           "AND (:currencyId IS NULL OR er.fromCurrencyId = :currencyId OR er.toCurrencyId = :currencyId) " +
           "AND (:currencyCode IS NULL OR fc.code = :currencyCode OR tc.code = :currencyCode) " +
           "AND (:startDate IS NULL OR er.effectiveDate >= :startDate) " +
           "AND (:endDate IS NULL OR er.effectiveDate <= :endDate) " +
           "AND (:source IS NULL OR er.source = :source)")
    Page<ExchangeRate> findRateHistory(@Param("currencyId") String currencyId,
                                      @Param("currencyCode") String currencyCode,
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