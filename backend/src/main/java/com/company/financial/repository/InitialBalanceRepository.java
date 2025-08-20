package com.company.financial.repository;

import com.company.financial.entity.InitialBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 期初余额数据访问接口
 */
@Repository
public interface InitialBalanceRepository extends JpaRepository<InitialBalance, String> {

    /**
     * 根据科目ID和会计年度查找
     */
    Optional<InitialBalance> findByAccountIdAndFiscalYear(String accountId, Integer fiscalYear);

    /**
     * 根据会计年度查找所有期初余额
     */
    List<InitialBalance> findByFiscalYear(Integer fiscalYear);

    /**
     * 根据会计年度和确认状态查找
     */
    List<InitialBalance> findByFiscalYearAndIsConfirmed(Integer fiscalYear, Boolean isConfirmed);

    /**
     * 分页查询期初余额
     */
    @Query("SELECT ib FROM InitialBalance ib " +
           "LEFT JOIN ib.account a " +
           "WHERE (:fiscalYear IS NULL OR ib.fiscalYear = :fiscalYear) " +
           "AND (:accountCode IS NULL OR a.code LIKE %:accountCode%) " +
           "AND (:accountName IS NULL OR a.name LIKE %:accountName%) " +
           "AND (:isConfirmed IS NULL OR ib.isConfirmed = :isConfirmed)")
    Page<InitialBalance> findByConditions(@Param("fiscalYear") Integer fiscalYear,
                                          @Param("accountCode") String accountCode,
                                          @Param("accountName") String accountName,
                                          @Param("isConfirmed") Boolean isConfirmed,
                                          Pageable pageable);

    /**
     * 计算借方合计
     */
    @Query("SELECT COALESCE(SUM(ib.beginningDebit), 0) FROM InitialBalance ib " +
           "WHERE ib.fiscalYear = :fiscalYear")
    BigDecimal sumBeginningDebitByFiscalYear(@Param("fiscalYear") Integer fiscalYear);

    /**
     * 计算贷方合计
     */
    @Query("SELECT COALESCE(SUM(ib.beginningCredit), 0) FROM InitialBalance ib " +
           "WHERE ib.fiscalYear = :fiscalYear")
    BigDecimal sumBeginningCreditByFiscalYear(@Param("fiscalYear") Integer fiscalYear);

    /**
     * 检查试算平衡 - 使用默认方法实现
     */
    default boolean checkTrialBalance(Integer fiscalYear) {
        BigDecimal totalDebit = sumBeginningDebitByFiscalYear(fiscalYear);
        BigDecimal totalCredit = sumBeginningCreditByFiscalYear(fiscalYear);
        return totalDebit.compareTo(totalCredit) == 0;
    }

    /**
     * 根据科目类型统计期初余额
     */
    @Query("SELECT a.accountType, " +
           "SUM(CASE WHEN a.balanceDirection = 'DEBIT' THEN ib.beginningBalance ELSE -ib.beginningBalance END) " +
           "FROM InitialBalance ib " +
           "JOIN ib.account a " +
           "WHERE ib.fiscalYear = :fiscalYear " +
           "GROUP BY a.accountType")
    List<Object[]> sumByAccountType(@Param("fiscalYear") Integer fiscalYear);

    /**
     * 批量确认期初余额
     */
    @Query("UPDATE InitialBalance ib SET ib.isConfirmed = true, ib.confirmedAt = CURRENT_TIMESTAMP, " +
           "ib.confirmedBy = :userId WHERE ib.fiscalYear = :fiscalYear AND ib.isConfirmed = false")
    @Modifying
    int confirmByFiscalYear(@Param("fiscalYear") Integer fiscalYear, @Param("userId") String userId);

    /**
     * 检查是否存在未确认的期初余额
     */
    boolean existsByFiscalYearAndIsConfirmed(Integer fiscalYear, Boolean isConfirmed);

    /**
     * 删除某年度的所有期初余额（用于重新导入）
     */
    void deleteByFiscalYear(Integer fiscalYear);
}