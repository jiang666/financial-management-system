package com.company.financial.service;

import com.company.financial.dto.account.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 会计科目服务接口
 */
public interface ChartOfAccountService {

    /**
     * 创建科目
     */
    AccountDetailDTO createAccount(AccountCreateDTO createDTO);

    /**
     * 更新科目
     */
    AccountDetailDTO updateAccount(AccountUpdateDTO updateDTO);

    /**
     * 删除科目
     */
    void deleteAccount(String id);

    /**
     * 根据ID获取科目详情
     */
    AccountDetailDTO getAccountById(String id);

    /**
     * 根据编码获取科目详情
     */
    AccountDetailDTO getAccountByCode(String code);

    /**
     * 分页查询科目
     */
    Page<AccountDetailDTO> queryAccounts(AccountQueryDTO queryDTO);

    /**
     * 获取科目树
     */
    List<AccountTreeDTO> getAccountTree();

    /**
     * 获取指定类型的科目树
     */
    List<AccountTreeDTO> getAccountTreeByType(String accountType);

    /**
     * 获取子科目列表
     */
    List<AccountDetailDTO> getChildAccounts(String parentId);

    /**
     * 搜索科目（根据编码或名称）
     */
    List<AccountDetailDTO> searchAccounts(String keyword);

    /**
     * 根据辅助核算类型获取科目
     */
    List<AccountDetailDTO> getAccountsByAuxiliaryType(String auxiliaryType);

    /**
     * 获取所有叶子节点科目
     */
    List<AccountDetailDTO> getLeafAccounts();

    /**
     * 生成下一个科目编码
     */
    String generateNextAccountCode(String parentCode);

    /**
     * 启用科目
     */
    void enableAccount(String id);

    /**
     * 停用科目
     */
    void disableAccount(String id);

    /**
     * 检查科目编码是否存在
     */
    boolean isAccountCodeExists(String code);

    /**
     * 检查科目是否可以删除
     */
    boolean canDeleteAccount(String id);

    /**
     * 批量导入科目
     */
    List<AccountDetailDTO> importAccounts(List<AccountCreateDTO> accounts);

    /**
     * 导出科目
     */
    List<AccountDetailDTO> exportAccounts(AccountQueryDTO queryDTO);
}