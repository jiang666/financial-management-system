package com.company.financial.repository;

import com.company.financial.entity.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 会计期间数据访问接口
 */
@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, String> {

    /**
     * 根据年度和期间查找
     */
    Optional<AccountingPeriod> findByFiscalYearAndPeriod(Integer fiscalYear, Integer period);

    /**
     * 根据年度查找所有期间
     */
    List<AccountingPeriod> findByFiscalYearOrderByPeriod(Integer fiscalYear);

    /**
     * 查找当前期间
     */
    Optional<AccountingPeriod> findByIsCurrentTrue();

    /**
     * 根据日期查找所属期间
     */
    @Query("SELECT ap FROM AccountingPeriod ap WHERE :date BETWEEN ap.startDate AND ap.endDate")
    Optional<AccountingPeriod> findByDate(@Param("date") LocalDate date);

    /**
     * 查找开放的期间
     */
    List<AccountingPeriod> findByStatusOrderByFiscalYearDescPeriodDesc(AccountingPeriod.PeriodStatus status);

    /**
     * 查找已结账的期间
     */
    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.status IN ('CLOSED', 'LOCKED') ORDER BY ap.fiscalYear DESC, ap.period DESC")
    List<AccountingPeriod> findClosedPeriods();

    /**
     * 获取最早的开放期间
     */
    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.status = 'OPEN' ORDER BY ap.fiscalYear, ap.period LIMIT 1")
    Optional<AccountingPeriod> findEarliestOpenPeriod();

    /**
     * 获取最新的已结账期间
     */
    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.status IN ('CLOSED', 'LOCKED') ORDER BY ap.fiscalYear DESC, ap.period DESC LIMIT 1")
    Optional<AccountingPeriod> findLatestClosedPeriod();

    /**
     * 检查是否存在未结账的期间（在指定期间之前）
     */
    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END FROM AccountingPeriod ap " +
           "WHERE ap.status = 'OPEN' AND " +
           "(ap.fiscalYear < :fiscalYear OR (ap.fiscalYear = :fiscalYear AND ap.period < :period))")
    boolean existsUnclosedPeriodsBefore(@Param("fiscalYear") Integer fiscalYear, @Param("period") Integer period);

    /**
     * 更新当前期间标志
     */
    @Query("UPDATE AccountingPeriod ap SET ap.isCurrent = CASE WHEN ap.id = :periodId THEN true ELSE false END")
    void updateCurrentPeriod(@Param("periodId") String periodId);

    /**
     * 获取指定年度的调整期
     */
    List<AccountingPeriod> findByFiscalYearAndIsAdjustmentTrue(Integer fiscalYear);

    /**
     * 检查期间是否可以记账
     */
    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END FROM AccountingPeriod ap " +
           "WHERE ap.id = :periodId AND ap.status = 'OPEN'")
    boolean isPeriodOpen(@Param("periodId") String periodId);
}