package com.company.financial.service.impl;

import com.company.financial.dto.account.InitialBalanceDTO;
import com.company.financial.dto.account.TrialBalanceDTO;
import com.company.financial.entity.ChartOfAccount;
import com.company.financial.entity.InitialBalance;
import com.company.financial.exception.BusinessException;
import com.company.financial.exception.ResourceNotFoundException;
import com.company.financial.repository.ChartOfAccountRepository;
import com.company.financial.repository.InitialBalanceRepository;
import com.company.financial.service.InitialBalanceService;
import com.company.financial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 期初余额服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InitialBalanceServiceImpl implements InitialBalanceService {

    private final InitialBalanceRepository balanceRepository;
    private final ChartOfAccountRepository accountRepository;

    @Override
    public InitialBalanceDTO saveInitialBalance(InitialBalanceDTO balanceDTO) {
        log.info("保存期初余额: 科目ID={}, 年度={}", balanceDTO.getAccountId(), balanceDTO.getFiscalYear());

        // 检查科目是否存在
        ChartOfAccount account = accountRepository.findById(balanceDTO.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));

        // 检查是否已确认
        if (isInitialBalanceConfirmed(balanceDTO.getFiscalYear())) {
            throw new BusinessException("该年度期初余额已确认，不能修改");
        }

        // 查找或创建期初余额
        InitialBalance balance = balanceRepository.findByAccountIdAndFiscalYear(
                balanceDTO.getAccountId(), balanceDTO.getFiscalYear())
                .orElse(new InitialBalance());

        // 设置基本信息
        balance.setAccountId(balanceDTO.getAccountId());
        balance.setFiscalYear(balanceDTO.getFiscalYear());
        balance.setCurrencyCode(balanceDTO.getCurrencyCode());
        balance.setAuxiliaryInfo(balanceDTO.getAuxiliaryInfo());

        // 根据余额方向设置金额
        if (account.getBalanceDirection() == ChartOfAccount.BalanceDirection.DEBIT) {
            // 借方余额科目
            balance.setBeginningBalance(balanceDTO.getBeginningBalance());
            balance.setBeginningDebit(balanceDTO.getBeginningBalance());
            balance.setBeginningCredit(BigDecimal.ZERO);
        } else {
            // 贷方余额科目
            balance.setBeginningBalance(balanceDTO.getBeginningBalance());
            balance.setBeginningDebit(BigDecimal.ZERO);
            balance.setBeginningCredit(balanceDTO.getBeginningBalance());
        }

        // 设置本年累计
        balance.setYtdDebit(balanceDTO.getYtdDebit() != null ? balanceDTO.getYtdDebit() : BigDecimal.ZERO);
        balance.setYtdCredit(balanceDTO.getYtdCredit() != null ? balanceDTO.getYtdCredit() : BigDecimal.ZERO);

        InitialBalance savedBalance = balanceRepository.save(balance);
        return convertToDTO(savedBalance, account);
    }

    @Override
    @Transactional
    public List<InitialBalanceDTO> batchSaveInitialBalances(List<InitialBalanceDTO> balances) {
        log.info("批量保存期初余额，数量: {}", balances.size());
        
        List<InitialBalanceDTO> savedBalances = new ArrayList<>();
        for (InitialBalanceDTO balanceDTO : balances) {
            try {
                InitialBalanceDTO saved = saveInitialBalance(balanceDTO);
                savedBalances.add(saved);
            } catch (Exception e) {
                log.error("保存期初余额失败: 科目ID={}, 错误: {}", 
                        balanceDTO.getAccountId(), e.getMessage());
            }
        }
        
        return savedBalances;
    }

    @Override
    public InitialBalanceDTO getInitialBalance(String accountId, Integer fiscalYear) {
        InitialBalance balance = balanceRepository.findByAccountIdAndFiscalYear(accountId, fiscalYear)
                .orElse(null);
        
        if (balance == null) {
            // 返回空的期初余额
            ChartOfAccount account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));
            return createEmptyBalance(account, fiscalYear);
        }
        
        ChartOfAccount account = accountRepository.findById(accountId).orElse(null);
        return convertToDTO(balance, account);
    }

    @Override
    public Page<InitialBalanceDTO> queryInitialBalances(Integer fiscalYear, String accountCode, 
                                                        String accountName, Boolean isConfirmed, 
                                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("account.code"));
        
        Page<InitialBalance> balancePage = balanceRepository.findByConditions(
                fiscalYear, accountCode, accountName, isConfirmed, pageable);
        
        return balancePage.map(balance -> {
            ChartOfAccount account = balance.getAccount();
            return convertToDTO(balance, account);
        });
    }

    @Override
    public List<InitialBalanceDTO> getInitialBalancesByYear(Integer fiscalYear) {
        List<InitialBalance> balances = balanceRepository.findByFiscalYear(fiscalYear);
        
        // 获取所有科目
        List<ChartOfAccount> allAccounts = accountRepository.findAllActiveAccounts();
        Map<String, ChartOfAccount> accountMap = allAccounts.stream()
                .collect(Collectors.toMap(ChartOfAccount::getId, a -> a));
        
        // 已有余额的科目ID集合
        Set<String> existingAccountIds = balances.stream()
                .map(InitialBalance::getAccountId)
                .collect(Collectors.toSet());
        
        // 转换已有余额
        List<InitialBalanceDTO> result = balances.stream()
                .map(balance -> convertToDTO(balance, accountMap.get(balance.getAccountId())))
                .collect(Collectors.toList());
        
        // 添加没有余额的科目（显示为0）
        for (ChartOfAccount account : allAccounts) {
            if (!existingAccountIds.contains(account.getId()) && account.getIsLeaf()) {
                result.add(createEmptyBalance(account, fiscalYear));
            }
        }
        
        // 按科目编码排序
        result.sort(Comparator.comparing(InitialBalanceDTO::getAccountCode));
        
        return result;
    }

    @Override
    public TrialBalanceDTO getTrialBalance(Integer fiscalYear) {
        log.info("获取试算平衡表: 年度={}", fiscalYear);
        
        // 计算借贷方合计
        BigDecimal totalDebit = balanceRepository.sumBeginningDebitByFiscalYear(fiscalYear);
        BigDecimal totalCredit = balanceRepository.sumBeginningCreditByFiscalYear(fiscalYear);
        BigDecimal difference = totalDebit.subtract(totalCredit);
        
        TrialBalanceDTO trialBalance = TrialBalanceDTO.builder()
                .fiscalYear(fiscalYear)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .difference(difference.abs())
                .isBalanced(difference.compareTo(BigDecimal.ZERO) == 0)
                .build();
        
        // 按科目类型统计
        List<Object[]> typeBalances = balanceRepository.sumByAccountType(fiscalYear);
        for (Object[] row : typeBalances) {
            ChartOfAccount.AccountType type = (ChartOfAccount.AccountType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            switch (type) {
                case ASSET:
                    trialBalance.setAssetTotal(amount);
                    break;
                case LIABILITY:
                    trialBalance.setLiabilityTotal(amount);
                    break;
                case EQUITY:
                    trialBalance.setEquityTotal(amount);
                    break;
                case COST:
                    trialBalance.setCostTotal(amount);
                    break;
                case REVENUE:
                    trialBalance.setRevenueTotal(amount);
                    break;
            }
        }
        
        // 查找不平衡的科目
        if (!trialBalance.getIsBalanced()) {
            List<TrialBalanceDTO.UnbalancedAccountDTO> unbalancedAccounts = findUnbalancedAccounts(fiscalYear);
            trialBalance.setUnbalancedAccounts(unbalancedAccounts);
        }
        
        return trialBalance;
    }

    @Override
    @Transactional
    public void confirmInitialBalances(Integer fiscalYear) {
        log.info("确认期初余额: 年度={}", fiscalYear);
        
        // 检查试算平衡
        TrialBalanceDTO trialBalance = getTrialBalance(fiscalYear);
        if (!trialBalance.getIsBalanced()) {
            throw new BusinessException("试算不平衡，请检查后再确认");
        }
        
        // 批量确认
        String currentUserId = SecurityUtils.getCurrentUserId();
        int count = balanceRepository.confirmByFiscalYear(fiscalYear, currentUserId);
        
        log.info("已确认{}条期初余额", count);
    }

    @Override
    @Transactional
    public void cancelConfirmInitialBalances(Integer fiscalYear) {
        log.info("取消确认期初余额: 年度={}", fiscalYear);
        
        // TODO: 检查是否已经开始记账
        
        List<InitialBalance> balances = balanceRepository.findByFiscalYearAndIsConfirmed(fiscalYear, true);
        for (InitialBalance balance : balances) {
            balance.setIsConfirmed(false);
            balance.setConfirmedAt(null);
            balance.setConfirmedBy(null);
        }
        
        balanceRepository.saveAll(balances);
        log.info("已取消确认{}条期初余额", balances.size());
    }

    @Override
    public void deleteInitialBalance(String id) {
        InitialBalance balance = balanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("期初余额不存在"));
        
        if (balance.getIsConfirmed()) {
            throw new BusinessException("已确认的期初余额不能删除");
        }
        
        balanceRepository.delete(balance);
        log.info("删除期初余额: ID={}", id);
    }

    @Override
    @Transactional
    public void deleteInitialBalancesByYear(Integer fiscalYear) {
        if (isInitialBalanceConfirmed(fiscalYear)) {
            throw new BusinessException("已确认的期初余额不能删除");
        }
        
        balanceRepository.deleteByFiscalYear(fiscalYear);
        log.info("删除{}年度所有期初余额", fiscalYear);
    }

    @Override
    @Transactional
    public List<InitialBalanceDTO> importInitialBalances(List<InitialBalanceDTO> balances, Integer fiscalYear) {
        log.info("导入期初余额: 年度={}, 数量={}", fiscalYear, balances.size());
        
        // 检查是否已确认
        if (isInitialBalanceConfirmed(fiscalYear)) {
            throw new BusinessException("该年度期初余额已确认，不能导入");
        }
        
        // 删除原有数据
        balanceRepository.deleteByFiscalYear(fiscalYear);
        
        // 批量保存
        return batchSaveInitialBalances(balances);
    }

    @Override
    public List<InitialBalanceDTO> exportInitialBalances(Integer fiscalYear) {
        return getInitialBalancesByYear(fiscalYear);
    }

    @Override
    @Transactional
    public void initializeNextYearBalances(Integer currentYear) {
        Integer nextYear = currentYear + 1;
        log.info("初始化下一年度期初余额: {}年 -> {}年", currentYear, nextYear);
        
        // 检查下一年度是否已有期初余额
        if (balanceRepository.existsByFiscalYearAndIsConfirmed(nextYear, false)) {
            throw new BusinessException("下一年度已存在未确认的期初余额");
        }
        
        // TODO: 从本年度期末余额生成下一年度期初余额
        // 这里需要根据实际业务逻辑实现
    }

    @Override
    public boolean isInitialBalanceConfirmed(Integer fiscalYear) {
        return !balanceRepository.existsByFiscalYearAndIsConfirmed(fiscalYear, false);
    }

    /**
     * 转换为DTO
     */
    private InitialBalanceDTO convertToDTO(InitialBalance balance, ChartOfAccount account) {
        InitialBalanceDTO dto = InitialBalanceDTO.builder()
                .id(balance.getId())
                .accountId(balance.getAccountId())
                .fiscalYear(balance.getFiscalYear())
                .beginningBalance(balance.getBeginningBalance())
                .beginningDebit(balance.getBeginningDebit())
                .beginningCredit(balance.getBeginningCredit())
                .ytdDebit(balance.getYtdDebit())
                .ytdCredit(balance.getYtdCredit())
                .currencyCode(balance.getCurrencyCode())
                .auxiliaryInfo(balance.getAuxiliaryInfo())
                .isConfirmed(balance.getIsConfirmed())
                .build();
        
        if (account != null) {
            dto.setAccountCode(account.getCode());
            dto.setAccountName(account.getName());
            dto.setAccountType(account.getAccountType().name());
            dto.setBalanceDirection(account.getBalanceDirection().name());
        }
        
        return dto;
    }

    /**
     * 创建空的期初余额
     */
    private InitialBalanceDTO createEmptyBalance(ChartOfAccount account, Integer fiscalYear) {
        return InitialBalanceDTO.builder()
                .accountId(account.getId())
                .accountCode(account.getCode())
                .accountName(account.getName())
                .accountType(account.getAccountType().name())
                .balanceDirection(account.getBalanceDirection().name())
                .fiscalYear(fiscalYear)
                .beginningBalance(BigDecimal.ZERO)
                .beginningDebit(BigDecimal.ZERO)
                .beginningCredit(BigDecimal.ZERO)
                .ytdDebit(BigDecimal.ZERO)
                .ytdCredit(BigDecimal.ZERO)
                .currencyCode("CNY")
                .isConfirmed(false)
                .build();
    }

    /**
     * 查找不平衡的科目
     */
    private List<TrialBalanceDTO.UnbalancedAccountDTO> findUnbalancedAccounts(Integer fiscalYear) {
        // TODO: 实现查找不平衡科目的逻辑
        return new ArrayList<>();
    }
}