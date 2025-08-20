package com.company.financial.controller;

import com.company.financial.common.ResponseEntity;
import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.account.*;
import com.company.financial.service.ChartOfAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 会计科目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class ChartOfAccountController {

    private final ChartOfAccountService accountService;

    /**
     * 创建科目
     */
    @PostMapping
    @PreAuthorize("hasPermission('ACCOUNT', 'CREATE')")
    public ResponseEntity<AccountDetailDTO> createAccount(@Valid @RequestBody AccountCreateDTO createDTO) {
        log.info("创建科目: {}", createDTO.getCode());
        AccountDetailDTO account = accountService.createAccount(createDTO);
        return ResponseEntity.success(account);
    }

    /**
     * 更新科目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('ACCOUNT', 'UPDATE')")
    public ResponseEntity<AccountDetailDTO> updateAccount(@PathVariable String id, 
                                                          @Valid @RequestBody AccountUpdateDTO updateDTO) {
        log.info("更新科目: {}", id);
        updateDTO.setId(id);
        AccountDetailDTO account = accountService.updateAccount(updateDTO);
        return ResponseEntity.success(account);
    }

    /**
     * 删除科目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('ACCOUNT', 'DELETE')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        log.info("删除科目: {}", id);
        accountService.deleteAccount(id);
        return ResponseEntity.success();
    }

    /**
     * 获取科目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<AccountDetailDTO> getAccount(@PathVariable String id) {
        AccountDetailDTO account = accountService.getAccountById(id);
        return ResponseEntity.success(account);
    }

    /**
     * 根据编码获取科目
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<AccountDetailDTO> getAccountByCode(@PathVariable String code) {
        AccountDetailDTO account = accountService.getAccountByCode(code);
        return ResponseEntity.success(account);
    }

    /**
     * 分页查询科目
     */
    @GetMapping
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<ResponsePageDataEntity<AccountDetailDTO>> queryAccounts(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) Boolean isLeaf,
            @RequestParam(required = false) String auxiliaryType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        AccountQueryDTO queryDTO = AccountQueryDTO.builder()
                .code(code)
                .name(name)
                .accountType(accountType)
                .level(level)
                .status(status)
                .parentId(parentId)
                .isLeaf(isLeaf)
                .auxiliaryType(auxiliaryType)
                .page(page)
                .size(size)
                .build();
        
        Page<AccountDetailDTO> pageResult = accountService.queryAccounts(queryDTO);
        
        ResponsePageDataEntity<AccountDetailDTO> pageData = new ResponsePageDataEntity<>();
        pageData.setTotal(pageResult.getTotalElements());
        pageData.setRows(pageResult.getContent());
        
        return ResponseEntity.success(pageData);
    }

    /**
     * 获取科目树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountTreeDTO>> getAccountTree() {
        List<AccountTreeDTO> tree = accountService.getAccountTree();
        return ResponseEntity.success(tree);
    }

    /**
     * 获取指定类型的科目树
     */
    @GetMapping("/tree/{accountType}")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountTreeDTO>> getAccountTreeByType(@PathVariable String accountType) {
        List<AccountTreeDTO> tree = accountService.getAccountTreeByType(accountType);
        return ResponseEntity.success(tree);
    }

    /**
     * 获取子科目列表
     */
    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountDetailDTO>> getChildAccounts(@PathVariable String parentId) {
        List<AccountDetailDTO> children = accountService.getChildAccounts(parentId);
        return ResponseEntity.success(children);
    }

    /**
     * 搜索科目
     */
    @GetMapping("/search")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountDetailDTO>> searchAccounts(@RequestParam String keyword) {
        List<AccountDetailDTO> accounts = accountService.searchAccounts(keyword);
        return ResponseEntity.success(accounts);
    }

    /**
     * 获取所有叶子节点科目
     */
    @GetMapping("/leaf")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountDetailDTO>> getLeafAccounts() {
        List<AccountDetailDTO> accounts = accountService.getLeafAccounts();
        return ResponseEntity.success(accounts);
    }

    /**
     * 生成下一个科目编码
     */
    @GetMapping("/next-code")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<String> generateNextAccountCode(@RequestParam(required = false) String parentCode) {
        String nextCode = accountService.generateNextAccountCode(parentCode);
        return ResponseEntity.success(nextCode);
    }

    /**
     * 启用科目
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasPermission('ACCOUNT', 'UPDATE')")
    public ResponseEntity<Void> enableAccount(@PathVariable String id) {
        log.info("启用科目: {}", id);
        accountService.enableAccount(id);
        return ResponseEntity.success();
    }

    /**
     * 停用科目
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasPermission('ACCOUNT', 'UPDATE')")
    public ResponseEntity<Void> disableAccount(@PathVariable String id) {
        log.info("停用科目: {}", id);
        accountService.disableAccount(id);
        return ResponseEntity.success();
    }

    /**
     * 检查科目编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<Boolean> checkAccountCode(@RequestParam String code) {
        boolean exists = accountService.isAccountCodeExists(code);
        return ResponseEntity.success(exists);
    }

    /**
     * 批量导入科目
     */
    @PostMapping("/import")
    @PreAuthorize("hasPermission('ACCOUNT', 'CREATE')")
    public ResponseEntity<List<AccountDetailDTO>> importAccounts(@Valid @RequestBody List<AccountCreateDTO> accounts) {
        log.info("批量导入科目，数量: {}", accounts.size());
        List<AccountDetailDTO> imported = accountService.importAccounts(accounts);
        return ResponseEntity.success(imported);
    }

    /**
     * 导出科目
     */
    @PostMapping("/export")
    @PreAuthorize("hasPermission('ACCOUNT', 'READ')")
    public ResponseEntity<List<AccountDetailDTO>> exportAccounts(@RequestBody AccountQueryDTO queryDTO) {
        log.info("导出科目");
        List<AccountDetailDTO> accounts = accountService.exportAccounts(queryDTO);
        return ResponseEntity.success(accounts);
    }
}