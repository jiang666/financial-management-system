package com.company.financial.repository;

import com.company.financial.entity.ChartOfAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会计科目数据访问接口
 */
@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, String> {

    /**
     * 根据科目编码查找
     */
    Optional<ChartOfAccount> findByCode(String code);

    /**
     * 根据科目编码查找（包含已删除的）
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE c.code = :code")
    Optional<ChartOfAccount> findByCodeIncludeDeleted(@Param("code") String code);

    /**
     * 根据父级ID查找子科目
     */
    List<ChartOfAccount> findByParentIdOrderByCode(String parentId);

    /**
     * 根据科目级次查找
     */
    List<ChartOfAccount> findByLevelOrderByCode(Integer level);

    /**
     * 根据科目类型查找
     */
    List<ChartOfAccount> findByAccountTypeOrderByCode(ChartOfAccount.AccountType accountType);

    /**
     * 根据助记码查找
     */
    List<ChartOfAccount> findByMemoCodeContainingIgnoreCase(String memoCode);

    /**
     * 模糊查询科目（根据编码或名称）
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE c.code LIKE %:keyword% OR c.name LIKE %:keyword% ORDER BY c.code")
    List<ChartOfAccount> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 分页查询科目
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE " +
           "(:code IS NULL OR c.code LIKE %:code%) AND " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:accountType IS NULL OR c.accountType = :accountType) AND " +
           "(:level IS NULL OR c.level = :level) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<ChartOfAccount> findByConditions(@Param("code") String code,
                                          @Param("name") String name,
                                          @Param("accountType") ChartOfAccount.AccountType accountType,
                                          @Param("level") Integer level,
                                          @Param("status") Integer status,
                                          Pageable pageable);

    /**
     * 查找所有叶子节点科目
     */
    List<ChartOfAccount> findByIsLeafTrueOrderByCode(Boolean isLeaf);

    /**
     * 检查科目编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查科目下是否有子科目
     */
    boolean existsByParentId(String parentId);

    /**
     * 根据路径查找科目
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE c.path LIKE :path% ORDER BY c.code")
    List<ChartOfAccount> findByPathStartingWith(@Param("path") String path);

    /**
     * 获取科目树（只包含启用的科目）
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE c.status = 1 ORDER BY c.code")
    List<ChartOfAccount> findAllActiveAccounts();

    /**
     * 根据辅助核算类型查找科目
     */
    @Query("SELECT c FROM ChartOfAccount c WHERE c.auxiliaryType LIKE %:auxiliaryType% ORDER BY c.code")
    List<ChartOfAccount> findByAuxiliaryType(@Param("auxiliaryType") String auxiliaryType);

    /**
     * 获取下一个可用的科目编码
     */
    @Query(value = "SELECT MAX(CAST(SUBSTRING(code, :startPos, :length) AS UNSIGNED)) " +
                   "FROM chart_of_accounts " +
                   "WHERE code LIKE :prefix% AND LENGTH(code) = :totalLength",
           nativeQuery = true)
    Integer findMaxCodeNumber(@Param("prefix") String prefix,
                              @Param("startPos") int startPos,
                              @Param("length") int length,
                              @Param("totalLength") int totalLength);
}