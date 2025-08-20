package com.company.financial.service;

import com.company.financial.dto.account.InitialBalanceDTO;
import com.company.financial.dto.account.TrialBalanceDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 期初余额服务接口
 */
public interface InitialBalanceService {

    /**
     * 保存期初余额
     */
    InitialBalanceDTO saveInitialBalance(InitialBalanceDTO balanceDTO);

    /**
     * 批量保存期初余额
     */
    List<InitialBalanceDTO> batchSaveInitialBalances(List<InitialBalanceDTO> balances);

    /**
     * 获取期初余额
     */
    InitialBalanceDTO getInitialBalance(String accountId, Integer fiscalYear);

    /**
     * 分页查询期初余额
     */
    Page<InitialBalanceDTO> queryInitialBalances(Integer fiscalYear, String accountCode, 
                                                  String accountName, Boolean isConfirmed, 
                                                  int page, int size);

    /**
     * 获取指定年度的所有期初余额
     */
    List<InitialBalanceDTO> getInitialBalancesByYear(Integer fiscalYear);

    /**
     * 试算平衡
     */
    TrialBalanceDTO getTrialBalance(Integer fiscalYear);

    /**
     * 确认期初余额
     */
    void confirmInitialBalances(Integer fiscalYear);

    /**
     * 取消确认期初余额
     */
    void cancelConfirmInitialBalances(Integer fiscalYear);

    /**
     * 删除期初余额
     */
    void deleteInitialBalance(String id);

    /**
     * 按年度删除期初余额
     */
    void deleteInitialBalancesByYear(Integer fiscalYear);

    /**
     * 导入期初余额
     */
    List<InitialBalanceDTO> importInitialBalances(List<InitialBalanceDTO> balances, Integer fiscalYear);

    /**
     * 导出期初余额
     */
    List<InitialBalanceDTO> exportInitialBalances(Integer fiscalYear);

    /**
     * 初始化下一年度期初余额
     */
    void initializeNextYearBalances(Integer currentYear);

    /**
     * 检查期初余额是否已确认
     */
    boolean isInitialBalanceConfirmed(Integer fiscalYear);
}