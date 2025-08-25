package com.company.financial.service;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.parameter.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 系统参数Service接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
public interface SystemParameterService {
    
    /**
     * 分页查询系统参数列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResponsePageDataEntity<SystemParameterDTO> getParameterList(SystemParameterQueryDTO queryDTO);
    
    /**
     * 根据参数键获取参数
     * 
     * @param key 参数键
     * @return 参数信息
     */
    SystemParameterDTO getParameterByKey(String key);
    
    /**
     * 更新参数
     * 
     * @param key 参数键
     * @param updateDTO 更新数据
     */
    void updateParameter(String key, SystemParameterUpdateDTO updateDTO);
    
    /**
     * 批量更新参数
     * 
     * @param batchUpdateDTO 批量更新数据
     * @return 更新结果
     */
    Map<String, Object> batchUpdateParameters(SystemParameterBatchUpdateDTO batchUpdateDTO);
    
    /**
     * 获取参数分类
     * 
     * @return 参数分类列表
     */
    List<ParameterCategoryDTO> getParameterCategories();
    
    /**
     * 导出参数配置
     * 
     * @param category 参数分类（可选）
     * @param response HTTP响应
     */
    void exportParameters(String category, HttpServletResponse response);
    
    /**
     * 导入参数配置
     * 
     * @param file 配置文件
     * @param overwrite 是否覆盖已存在的参数
     * @return 导入结果
     */
    Map<String, Object> importParameters(MultipartFile file, Boolean overwrite);
    
    /**
     * 获取参数修改历史
     * 
     * @param key 参数键
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    ResponsePageDataEntity<ParameterHistoryDTO> getParameterHistory(String key, Long startTime, Long endTime, Integer page, Integer size);
    
    /**
     * 验证参数值
     * 
     * @param validateDTO 验证数据
     * @return 验证结果
     */
    Map<String, Object> validateParameter(ParameterValidateDTO validateDTO);
    
    /**
     * 刷新参数缓存
     * 
     * @param category 参数分类（可选）
     * @return 刷新结果
     */
    Map<String, Object> refreshParameterCache(String category);
    
    /**
     * 根据参数键获取参数值
     * 
     * @param key 参数键
     * @return 参数值
     */
    String getParameterValue(String key);
    
    /**
     * 根据参数键获取参数值，如果不存在则返回默认值
     * 
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getParameterValue(String key, String defaultValue);
    
    /**
     * 获取整数类型参数值
     * 
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    Integer getIntegerParameter(String key, Integer defaultValue);
    
    /**
     * 获取布尔类型参数值
     * 
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    Boolean getBooleanParameter(String key, Boolean defaultValue);
}