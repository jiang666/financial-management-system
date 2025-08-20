package com.company.financial.service.impl;

import com.company.financial.dto.account.*;
import com.company.financial.entity.AuxiliaryType;
import com.company.financial.entity.ChartOfAccount;
import com.company.financial.exception.BusinessException;
import com.company.financial.exception.ResourceNotFoundException;
import com.company.financial.repository.ChartOfAccountRepository;
import com.company.financial.repository.InitialBalanceRepository;
import com.company.financial.service.ChartOfAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 会计科目服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChartOfAccountServiceImpl implements ChartOfAccountService {

    private final ChartOfAccountRepository accountRepository;
    private final InitialBalanceRepository balanceRepository;

    @Override
    public AccountDetailDTO createAccount(AccountCreateDTO createDTO) {
        log.info("创建科目: {}", createDTO.getCode());

        // 检查科目编码是否已存在
        if (accountRepository.existsByCode(createDTO.getCode())) {
            throw new BusinessException("科目编码已存在: " + createDTO.getCode());
        }

        ChartOfAccount account = new ChartOfAccount();
        account.setCode(createDTO.getCode());
        account.setName(createDTO.getName());
        account.setEnglishName(createDTO.getEnglishName());
        account.setAccountType(ChartOfAccount.AccountType.valueOf(createDTO.getAccountType()));
        account.setBalanceDirection(ChartOfAccount.BalanceDirection.valueOf(createDTO.getBalanceDirection()));
        account.setMemoCode(createDTO.getMemoCode());
        account.setCurrencyCode(createDTO.getCurrencyCode());
        account.setIsQuantity(createDTO.getIsQuantity());
        account.setUnit(createDTO.getUnit());
        account.setStatus(createDTO.getStatus());

        // 处理辅助核算类型
        if (createDTO.getAuxiliaryTypes() != null && !createDTO.getAuxiliaryTypes().isEmpty()) {
            account.setAuxiliaryType(String.join(",", createDTO.getAuxiliaryTypes()));
        }

        // 处理父级科目
        if (StringUtils.hasText(createDTO.getParentId())) {
            ChartOfAccount parent = accountRepository.findById(createDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父级科目不存在"));
            
            account.setParentId(parent.getId());
            account.setLevel(parent.getLevel() + 1);
            account.setPath(parent.getPath() + "/" + account.getCode());
            account.setFullName(parent.getFullName() + "/" + account.getName());
            
            // 更新父级科目的叶子节点标志
            parent.setIsLeaf(false);
            accountRepository.save(parent);
        } else {
            // 一级科目
            account.setLevel(1);
            account.setPath(account.getCode());
            account.setFullName(account.getName());
        }

        ChartOfAccount savedAccount = accountRepository.save(account);
        return convertToDetailDTO(savedAccount);
    }

    @Override
    public AccountDetailDTO updateAccount(AccountUpdateDTO updateDTO) {
        log.info("更新科目: {}", updateDTO.getId());

        ChartOfAccount account = accountRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));

        // 更新基本信息
        account.setName(updateDTO.getName());
        account.setEnglishName(updateDTO.getEnglishName());
        account.setMemoCode(updateDTO.getMemoCode());
        account.setCurrencyCode(updateDTO.getCurrencyCode());
        account.setIsQuantity(updateDTO.getIsQuantity());
        account.setUnit(updateDTO.getUnit());
        account.setStatus(updateDTO.getStatus());

        // 更新辅助核算类型
        if (updateDTO.getAuxiliaryTypes() != null) {
            account.setAuxiliaryType(String.join(",", updateDTO.getAuxiliaryTypes()));
        }

        // 更新完整名称
        if (StringUtils.hasText(account.getParentId())) {
            ChartOfAccount parent = accountRepository.findById(account.getParentId()).orElse(null);
            if (parent != null) {
                account.setFullName(parent.getFullName() + "/" + account.getName());
            }
        } else {
            account.setFullName(account.getName());
        }

        ChartOfAccount savedAccount = accountRepository.save(account);
        return convertToDetailDTO(savedAccount);
    }

    @Override
    public void deleteAccount(String id) {
        log.info("删除科目: {}", id);

        ChartOfAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));

        // 检查是否可以删除
        if (!canDeleteAccount(id)) {
            throw new BusinessException("科目不能删除，存在子科目或已被使用");
        }

        // 软删除
        account.setDeleted(true);
        accountRepository.save(account);

        // 更新父级科目的叶子节点标志
        if (StringUtils.hasText(account.getParentId())) {
            boolean hasOtherChildren = accountRepository.existsByParentId(account.getParentId());
            if (!hasOtherChildren) {
                ChartOfAccount parent = accountRepository.findById(account.getParentId()).orElse(null);
                if (parent != null) {
                    parent.setIsLeaf(true);
                    accountRepository.save(parent);
                }
            }
        }
    }

    @Override
    public AccountDetailDTO getAccountById(String id) {
        ChartOfAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));
        return convertToDetailDTO(account);
    }

    @Override
    public AccountDetailDTO getAccountByCode(String code) {
        ChartOfAccount account = accountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));
        return convertToDetailDTO(account);
    }

    @Override
    public Page<AccountDetailDTO> queryAccounts(AccountQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), Sort.by("code"));
        
        ChartOfAccount.AccountType accountType = null;
        if (StringUtils.hasText(queryDTO.getAccountType())) {
            accountType = ChartOfAccount.AccountType.valueOf(queryDTO.getAccountType());
        }

        Page<ChartOfAccount> page = accountRepository.findByConditions(
                queryDTO.getCode(),
                queryDTO.getName(),
                accountType,
                queryDTO.getLevel(),
                queryDTO.getStatus(),
                pageable
        );

        return page.map(this::convertToDetailDTO);
    }

    @Override
    public List<AccountTreeDTO> getAccountTree() {
        List<ChartOfAccount> allAccounts = accountRepository.findAllActiveAccounts();
        return buildAccountTree(allAccounts, null);
    }

    @Override
    public List<AccountTreeDTO> getAccountTreeByType(String accountType) {
        ChartOfAccount.AccountType type = ChartOfAccount.AccountType.valueOf(accountType);
        List<ChartOfAccount> accounts = accountRepository.findByAccountTypeOrderByCode(type);
        return buildAccountTree(accounts, null);
    }

    @Override
    public List<AccountDetailDTO> getChildAccounts(String parentId) {
        List<ChartOfAccount> children = accountRepository.findByParentIdOrderByCode(parentId);
        return children.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDetailDTO> searchAccounts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        
        List<ChartOfAccount> accounts = accountRepository.searchByKeyword(keyword);
        return accounts.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDetailDTO> getAccountsByAuxiliaryType(String auxiliaryType) {
        List<ChartOfAccount> accounts = accountRepository.findByAuxiliaryType(auxiliaryType);
        return accounts.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDetailDTO> getLeafAccounts() {
        List<ChartOfAccount> accounts = accountRepository.findByIsLeafTrueOrderByCode(true);
        return accounts.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String generateNextAccountCode(String parentCode) {
        if (!StringUtils.hasText(parentCode)) {
            // 生成一级科目编码
            Integer maxCode = accountRepository.findMaxCodeNumber("", 1, 4, 4);
            int nextCode = (maxCode == null ? 1000 : maxCode) + 1;
            return String.valueOf(nextCode);
        }

        // 生成子科目编码
        ChartOfAccount parent = accountRepository.findByCode(parentCode)
                .orElseThrow(() -> new ResourceNotFoundException("父级科目不存在"));
        
        int codeLength = getCodeLengthByLevel(parent.getLevel() + 1);
        String prefix = parent.getCode();
        int totalLength = prefix.length() + codeLength;
        
        Integer maxCode = accountRepository.findMaxCodeNumber(
                prefix, 
                prefix.length() + 1, 
                codeLength, 
                totalLength
        );
        
        int nextCode = (maxCode == null ? 0 : maxCode) + 1;
        return prefix + String.format("%0" + codeLength + "d", nextCode);
    }

    @Override
    public void enableAccount(String id) {
        ChartOfAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));
        account.setStatus(1);
        accountRepository.save(account);
        log.info("启用科目: {}", account.getCode());
    }

    @Override
    public void disableAccount(String id) {
        ChartOfAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("科目不存在"));
        
        // 检查是否有子科目
        if (accountRepository.existsByParentId(id)) {
            throw new BusinessException("存在子科目，不能停用");
        }
        
        account.setStatus(0);
        accountRepository.save(account);
        log.info("停用科目: {}", account.getCode());
    }

    @Override
    public boolean isAccountCodeExists(String code) {
        return accountRepository.existsByCode(code);
    }

    @Override
    public boolean canDeleteAccount(String id) {
        // 检查是否有子科目
        if (accountRepository.existsByParentId(id)) {
            return false;
        }
        
        // 检查是否有期初余额
        if (balanceRepository.findByAccountIdAndFiscalYear(id, getCurrentYear()).isPresent()) {
            return false;
        }
        
        // TODO: 检查是否在凭证中使用
        
        return true;
    }

    @Override
    @Transactional
    public List<AccountDetailDTO> importAccounts(List<AccountCreateDTO> accounts) {
        log.info("批量导入科目，数量: {}", accounts.size());
        
        List<AccountDetailDTO> imported = new ArrayList<>();
        for (AccountCreateDTO createDTO : accounts) {
            try {
                AccountDetailDTO account = createAccount(createDTO);
                imported.add(account);
            } catch (Exception e) {
                log.error("导入科目失败: {}, 错误: {}", createDTO.getCode(), e.getMessage());
            }
        }
        
        return imported;
    }

    @Override
    public List<AccountDetailDTO> exportAccounts(AccountQueryDTO queryDTO) {
        // 设置导出不分页
        queryDTO.setPage(0);
        queryDTO.setSize(Integer.MAX_VALUE);
        
        Page<AccountDetailDTO> page = queryAccounts(queryDTO);
        return page.getContent();
    }

    /**
     * 构建科目树
     */
    private List<AccountTreeDTO> buildAccountTree(List<ChartOfAccount> accounts, String parentId) {
        List<AccountTreeDTO> tree = new ArrayList<>();
        
        for (ChartOfAccount account : accounts) {
            boolean matches = (parentId == null && account.getParentId() == null) ||
                    (parentId != null && parentId.equals(account.getParentId()));
            
            if (matches) {
                AccountTreeDTO node = convertToTreeDTO(account);
                List<AccountTreeDTO> children = buildAccountTree(accounts, account.getId());
                node.setChildren(children);
                node.setHasChildren(!children.isEmpty());
                tree.add(node);
            }
        }
        
        return tree;
    }

    /**
     * 转换为详情DTO
     */
    private AccountDetailDTO convertToDetailDTO(ChartOfAccount account) {
        AccountDetailDTO dto = AccountDetailDTO.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .englishName(account.getEnglishName())
                .parentId(account.getParentId())
                .level(account.getLevel())
                .accountType(account.getAccountType().name())
                .accountTypeName(account.getAccountType().getDescription())
                .balanceDirection(account.getBalanceDirection().name())
                .balanceDirectionName(account.getBalanceDirection().getDescription())
                .isLeaf(account.getIsLeaf())
                .status(account.getStatus())
                .memoCode(account.getMemoCode())
                .currencyCode(account.getCurrencyCode())
                .isQuantity(account.getIsQuantity())
                .unit(account.getUnit())
                .path(account.getPath())
                .fullName(account.getFullName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .createdBy(account.getCreatedBy())
                .updatedBy(account.getUpdatedBy())
                .build();

        // 处理辅助核算类型
        if (StringUtils.hasText(account.getAuxiliaryType())) {
            dto.setAuxiliaryTypes(Arrays.asList(account.getAuxiliaryType().split(",")));
        }

        // 获取父级科目信息
        if (StringUtils.hasText(account.getParentId())) {
            accountRepository.findById(account.getParentId()).ifPresent(parent -> {
                dto.setParentCode(parent.getCode());
                dto.setParentName(parent.getName());
            });
        }

        // 获取子科目数量
        int childrenCount = accountRepository.findByParentIdOrderByCode(account.getId()).size();
        dto.setChildrenCount(childrenCount);

        return dto;
    }

    /**
     * 转换为树形DTO
     */
    private AccountTreeDTO convertToTreeDTO(ChartOfAccount account) {
        AccountTreeDTO dto = AccountTreeDTO.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .label(account.getCode() + " " + account.getName())
                .parentId(account.getParentId())
                .level(account.getLevel())
                .accountType(account.getAccountType().name())
                .balanceDirection(account.getBalanceDirection().name())
                .isLeaf(account.getIsLeaf())
                .status(account.getStatus())
                .disabled(account.getStatus() == 0)
                .build();

        // 处理辅助核算类型
        if (StringUtils.hasText(account.getAuxiliaryType())) {
            dto.setAuxiliaryTypes(Arrays.asList(account.getAuxiliaryType().split(",")));
        }

        return dto;
    }

    /**
     * 根据科目级次获取编码长度
     */
    private int getCodeLengthByLevel(int level) {
        // 默认编码规则: 4-2-2-2
        switch (level) {
            case 1:
                return 4;
            case 2:
            case 3:
            case 4:
                return 2;
            default:
                return 2;
        }
    }

    /**
     * 获取当前会计年度
     */
    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}