package com.company.financial.controller;

import com.company.financial.common.ResponseEntity;
import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.account.InitialBalanceDTO;
import com.company.financial.dto.account.TrialBalanceDTO;
import com.company.financial.service.InitialBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 期初余额管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/initial-balances")
@RequiredArgsConstructor
@Validated
public class InitialBalanceController {

    private final InitialBalanceService balanceService;

    /**
     * 保存期初余额
     */
    @PostMapping
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CREATE')")
    public ResponseEntity<InitialBalanceDTO> saveInitialBalance(@Valid @RequestBody InitialBalanceDTO balanceDTO) {
        log.info("保存期初余额: 科目ID={}, 年度={}", balanceDTO.getAccountId(), balanceDTO.getFiscalYear());
        InitialBalanceDTO saved = balanceService.saveInitialBalance(balanceDTO);
        return ResponseEntity.success(saved);
    }

    /**
     * 批量保存期初余额
     */
    @PostMapping("/batch")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CREATE')")
    public ResponseEntity<List<InitialBalanceDTO>> batchSaveInitialBalances(@Valid @RequestBody List<InitialBalanceDTO> balances) {
        log.info("批量保存期初余额，数量: {}", balances.size());
        List<InitialBalanceDTO> saved = balanceService.batchSaveInitialBalances(balances);
        return ResponseEntity.success(saved);
    }

    /**
     * 获取期初余额
     */
    @GetMapping("/{accountId}/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<InitialBalanceDTO> getInitialBalance(@PathVariable String accountId, 
                                                               @PathVariable Integer fiscalYear) {
        InitialBalanceDTO balance = balanceService.getInitialBalance(accountId, fiscalYear);
        return ResponseEntity.success(balance);
    }

    /**
     * 分页查询期初余额
     */
    @GetMapping
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<ResponsePageDataEntity<InitialBalanceDTO>> queryInitialBalances(
            @RequestParam(required = false) Integer fiscalYear,
            @RequestParam(required = false) String accountCode,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) Boolean isConfirmed,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<InitialBalanceDTO> pageResult = balanceService.queryInitialBalances(
                fiscalYear, accountCode, accountName, isConfirmed, page, size);
        
        ResponsePageDataEntity<InitialBalanceDTO> pageData = new ResponsePageDataEntity<>();
        pageData.setTotal(pageResult.getTotalElements());
        pageData.setRows(pageResult.getContent());
        
        return ResponseEntity.success(pageData);
    }

    /**
     * 获取指定年度的所有期初余额
     */
    @GetMapping("/year/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<List<InitialBalanceDTO>> getInitialBalancesByYear(@PathVariable Integer fiscalYear) {
        List<InitialBalanceDTO> balances = balanceService.getInitialBalancesByYear(fiscalYear);
        return ResponseEntity.success(balances);
    }

    /**
     * 试算平衡
     */
    @GetMapping("/trial-balance/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<TrialBalanceDTO> getTrialBalance(@PathVariable Integer fiscalYear) {
        log.info("获取试算平衡表: 年度={}", fiscalYear);
        TrialBalanceDTO trialBalance = balanceService.getTrialBalance(fiscalYear);
        return ResponseEntity.success(trialBalance);
    }

    /**
     * 确认期初余额
     */
    @PostMapping("/confirm/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CONFIRM')")
    public ResponseEntity<Void> confirmInitialBalances(@PathVariable Integer fiscalYear) {
        log.info("确认期初余额: 年度={}", fiscalYear);
        balanceService.confirmInitialBalances(fiscalYear);
        return ResponseEntity.success();
    }

    /**
     * 取消确认期初余额
     */
    @PostMapping("/cancel-confirm/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CONFIRM')")
    public ResponseEntity<Void> cancelConfirmInitialBalances(@PathVariable Integer fiscalYear) {
        log.info("取消确认期初余额: 年度={}", fiscalYear);
        balanceService.cancelConfirmInitialBalances(fiscalYear);
        return ResponseEntity.success();
    }

    /**
     * 删除期初余额
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'DELETE')")
    public ResponseEntity<Void> deleteInitialBalance(@PathVariable String id) {
        log.info("删除期初余额: {}", id);
        balanceService.deleteInitialBalance(id);
        return ResponseEntity.success();
    }

    /**
     * 按年度删除期初余额
     */
    @DeleteMapping("/year/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'DELETE')")
    public ResponseEntity<Void> deleteInitialBalancesByYear(@PathVariable Integer fiscalYear) {
        log.info("删除{}年度所有期初余额", fiscalYear);
        balanceService.deleteInitialBalancesByYear(fiscalYear);
        return ResponseEntity.success();
    }

    /**
     * 导入期初余额
     */
    @PostMapping("/import/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CREATE')")
    public ResponseEntity<List<InitialBalanceDTO>> importInitialBalances(@PathVariable Integer fiscalYear,
                                                                         @Valid @RequestBody List<InitialBalanceDTO> balances) {
        log.info("导入期初余额: 年度={}, 数量={}", fiscalYear, balances.size());
        List<InitialBalanceDTO> imported = balanceService.importInitialBalances(balances, fiscalYear);
        return ResponseEntity.success(imported);
    }

    /**
     * 导出期初余额
     */
    @GetMapping("/export/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<List<InitialBalanceDTO>> exportInitialBalances(@PathVariable Integer fiscalYear) {
        log.info("导出期初余额: 年度={}", fiscalYear);
        List<InitialBalanceDTO> balances = balanceService.exportInitialBalances(fiscalYear);
        return ResponseEntity.success(balances);
    }

    /**
     * 初始化下一年度期初余额
     */
    @PostMapping("/initialize/{currentYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'CREATE')")
    public ResponseEntity<Void> initializeNextYearBalances(@PathVariable Integer currentYear) {
        log.info("初始化下一年度期初余额: 当前年度={}", currentYear);
        balanceService.initializeNextYearBalances(currentYear);
        return ResponseEntity.success();
    }

    /**
     * 检查期初余额是否已确认
     */
    @GetMapping("/check-confirmed/{fiscalYear}")
    @PreAuthorize("hasPermission('INITIAL_BALANCE', 'READ')")
    public ResponseEntity<Boolean> checkInitialBalanceConfirmed(@PathVariable Integer fiscalYear) {
        boolean confirmed = balanceService.isInitialBalanceConfirmed(fiscalYear);
        return ResponseEntity.success(confirmed);
    }
}