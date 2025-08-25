package com.company.financial.service.impl;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.parameter.*;
import com.company.financial.entity.SystemParameter;
import com.company.financial.enums.ParamCategory;
import com.company.financial.enums.ParamType;
import com.company.financial.exception.BusinessException;
import com.company.financial.exception.DataNotFoundException;
import com.company.financial.exception.ValidationException;
import com.company.financial.repository.SystemParameterRepository;
import com.company.financial.service.SystemParameterService;
import com.company.financial.utils.UUIDUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统参数Service实现类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemParameterServiceImpl implements SystemParameterService {
    
    private final SystemParameterRepository systemParameterRepository;
    private final ObjectMapper objectMapper;
    
    private static final String CACHE_NAME = "systemParameter";
    
    @Override
    public ResponsePageDataEntity<SystemParameterDTO> getParameterList(SystemParameterQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize());
        
        Page<SystemParameter> page = systemParameterRepository.findByConditions(
                queryDTO.getCategory(),
                queryDTO.getKeyword(),
                queryDTO.getIsSystem(),
                queryDTO.getVisible(),
                0,
                pageable
        );
        
        List<SystemParameterDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        ResponsePageDataEntity<SystemParameterDTO> result = new ResponsePageDataEntity<>();
        result.setContent(dtoList);
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setNumber(page.getNumber());
        result.setSize(page.getSize());
        result.setNumberOfElements(page.getNumberOfElements());
        result.setFirst(page.isFirst());
        result.setLast(page.isLast());
        return result;
    }
    
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public SystemParameterDTO getParameterByKey(String key) {
        SystemParameter parameter = systemParameterRepository.findByParamKeyAndDeleted(key, 0)
                .orElseThrow(() -> new DataNotFoundException("参数不存在：" + key));
        
        return convertToDTO(parameter);
    }
    
    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME, key = "#key")
    public void updateParameter(String key, SystemParameterUpdateDTO updateDTO) {
        SystemParameter parameter = systemParameterRepository.findByParamKeyAndDeleted(key, 0)
                .orElseThrow(() -> new DataNotFoundException("参数不存在：" + key));
        
        // 检查权限
        if (parameter.getIsSystem() && !isAdmin()) {
            throw new BusinessException(403, "系统参数只有超级管理员可以修改");
        }
        
        if (!parameter.getEditable()) {
            throw new BusinessException(403, "该参数不可编辑");
        }
        
        // 验证参数值
        validateParameterValue(parameter, updateDTO.getParamValue());
        
        // 保存旧值用于历史记录
        String oldValue = parameter.getParamValue();
        
        // 更新参数
        parameter.setParamValue(updateDTO.getParamValue());
        if (updateDTO.getDescription() != null) {
            parameter.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getValidationRule() != null) {
            parameter.setValidationRule(updateDTO.getValidationRule());
        }
        if (updateDTO.getMinValue() != null) {
            parameter.setMinValue(updateDTO.getMinValue());
        }
        if (updateDTO.getMaxValue() != null) {
            parameter.setMaxValue(updateDTO.getMaxValue());
        }
        if (updateDTO.getOptions() != null) {
            parameter.setOptions(updateDTO.getOptions());
        }
        if (updateDTO.getDefaultValue() != null) {
            parameter.setDefaultValue(updateDTO.getDefaultValue());
        }
        if (updateDTO.getRequired() != null) {
            parameter.setRequired(updateDTO.getRequired());
        }
        if (updateDTO.getVisible() != null) {
            parameter.setVisible(updateDTO.getVisible());
        }
        if (updateDTO.getEditable() != null) {
            parameter.setEditable(updateDTO.getEditable());
        }
        if (updateDTO.getEffectiveImmediately() != null) {
            parameter.setEffectiveImmediately(updateDTO.getEffectiveImmediately());
        }
        if (updateDTO.getRestartRequired() != null) {
            parameter.setRestartRequired(updateDTO.getRestartRequired());
        }
        
        parameter.setUpdatedAt(System.currentTimeMillis());
        parameter.setUpdatedBy(getCurrentUser());
        
        systemParameterRepository.save(parameter);
        
        // TODO: 记录历史
        // saveParameterHistory(parameter, oldValue, "UPDATE");
        
        log.info("更新系统参数：key={}, oldValue={}, newValue={}", key, oldValue, parameter.getParamValue());
    }
    
    @Override
    @Transactional
    public Map<String, Object> batchUpdateParameters(SystemParameterBatchUpdateDTO batchUpdateDTO) {
        int successCount = 0;
        int failureCount = 0;
        List<Map<String, String>> failureDetails = new ArrayList<>();
        
        for (SystemParameterBatchUpdateDTO.ParameterItem item : batchUpdateDTO.getParameters()) {
            try {
                SystemParameterUpdateDTO updateDTO = new SystemParameterUpdateDTO();
                updateDTO.setParamValue(item.getParamValue());
                updateParameter(item.getParamKey(), updateDTO);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                Map<String, String> detail = new HashMap<>();
                detail.put("paramKey", item.getParamKey());
                detail.put("reason", e.getMessage());
                failureDetails.add(detail);
                log.error("批量更新参数失败：key={}, error={}", item.getParamKey(), e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("failureDetails", failureDetails);
        
        return result;
    }
    
    @Override
    public List<ParameterCategoryDTO> getParameterCategories() {
        List<ParameterCategoryDTO> categories = new ArrayList<>();
        
        // 基础设置
        ParameterCategoryDTO basic = new ParameterCategoryDTO();
        basic.setCode("BASIC");
        basic.setName("基础设置");
        basic.setIcon("Setting");
        basic.setSortOrder(1);
        
        List<ParameterCategoryDTO> basicSub = new ArrayList<>();
        
        ParameterCategoryDTO companyInfo = new ParameterCategoryDTO();
        companyInfo.setCode("COMPANY_INFO");
        companyInfo.setName("企业信息");
        companyInfo.setParameterKeys(Arrays.asList(
                "company.name", "company.code", "company.tax_number",
                "company.legal_person", "company.address", "company.phone"
        ));
        basicSub.add(companyInfo);
        
        ParameterCategoryDTO accountingPolicy = new ParameterCategoryDTO();
        accountingPolicy.setCode("ACCOUNTING_POLICY");
        accountingPolicy.setName("会计制度");
        accountingPolicy.setParameterKeys(Arrays.asList(
                "accounting.standard", "accounting.currency", "accounting.start_date"
        ));
        basicSub.add(accountingPolicy);
        
        basic.setSubcategories(basicSub);
        categories.add(basic);
        
        // 财务核算
        ParameterCategoryDTO financial = new ParameterCategoryDTO();
        financial.setCode("FINANCIAL");
        financial.setName("财务核算");
        financial.setIcon("Money");
        financial.setSortOrder(2);
        
        List<ParameterCategoryDTO> financialSub = new ArrayList<>();
        
        ParameterCategoryDTO voucher = new ParameterCategoryDTO();
        voucher.setCode("VOUCHER");
        voucher.setName("凭证设置");
        voucher.setParameterKeys(Arrays.asList(
                "voucher.auto_number", "voucher.number_rule",
                "voucher.approval_flow", "voucher.abstract_library"
        ));
        financialSub.add(voucher);
        
        financial.setSubcategories(financialSub);
        categories.add(financial);
        
        // 系统管理
        ParameterCategoryDTO system = new ParameterCategoryDTO();
        system.setCode("SYSTEM");
        system.setName("系统管理");
        system.setIcon("Setting");
        system.setSortOrder(3);
        
        List<ParameterCategoryDTO> systemSub = new ArrayList<>();
        
        ParameterCategoryDTO security = new ParameterCategoryDTO();
        security.setCode("SECURITY");
        security.setName("安全设置");
        security.setParameterKeys(Arrays.asList(
                "password.min_length", "password.expire_days",
                "login.max_attempts", "session.timeout"
        ));
        systemSub.add(security);
        
        system.setSubcategories(systemSub);
        categories.add(system);
        
        return categories;
    }
    
    @Override
    public void exportParameters(String category, HttpServletResponse response) {
        try {
            List<SystemParameter> parameters;
            if (StringUtils.hasText(category)) {
                parameters = systemParameterRepository.findByCategoryAndDeletedOrderBySortOrderAsc(category, 0);
            } else {
                parameters = systemParameterRepository.findByVisibleAndDeletedOrderByCategoryAscSortOrderAsc(true, 0);
            }
            
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("exportTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            exportData.put("version", "1.0");
            
            List<Map<String, Object>> paramList = parameters.stream().map(param -> {
                Map<String, Object> map = new HashMap<>();
                map.put("paramKey", param.getParamKey());
                map.put("paramValue", param.getParamValue());
                map.put("paramType", param.getParamType());
                map.put("category", param.getCategory());
                map.put("description", param.getDescription());
                map.put("isSystem", param.getIsSystem());
                map.put("isEncrypted", param.getIsEncrypted());
                map.put("validationRule", param.getValidationRule());
                map.put("minValue", param.getMinValue());
                map.put("maxValue", param.getMaxValue());
                map.put("options", param.getOptions());
                map.put("defaultValue", param.getDefaultValue());
                map.put("required", param.getRequired());
                map.put("visible", param.getVisible());
                map.put("editable", param.getEditable());
                map.put("effectiveImmediately", param.getEffectiveImmediately());
                map.put("restartRequired", param.getRestartRequired());
                return map;
            }).collect(Collectors.toList());
            
            exportData.put("parameters", paramList);
            
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
            
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=system_parameters_" + 
                    System.currentTimeMillis() + ".json");
            response.getOutputStream().write(jsonContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            
        } catch (IOException e) {
            log.error("导出参数配置失败", e);
            throw new BusinessException(500, "导出失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> importParameters(MultipartFile file, Boolean overwrite) {
        if (overwrite == null) {
            overwrite = false;
        }
        
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> importData = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            
            List<Map<String, Object>> paramList = (List<Map<String, Object>>) importData.get("parameters");
            if (paramList == null || paramList.isEmpty()) {
                throw new ValidationException("导入文件中没有参数数据");
            }
            
            int totalCount = paramList.size();
            int successCount = 0;
            int failureCount = 0;
            List<Map<String, String>> failureDetails = new ArrayList<>();
            
            for (Map<String, Object> paramMap : paramList) {
                try {
                    String paramKey = (String) paramMap.get("paramKey");
                    if (!StringUtils.hasText(paramKey)) {
                        throw new ValidationException("参数键不能为空");
                    }
                    
                    Optional<SystemParameter> existingOpt = systemParameterRepository.findByParamKeyAndDeleted(paramKey, 0);
                    
                    if (existingOpt.isPresent() && !overwrite) {
                        failureCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("paramKey", paramKey);
                        detail.put("reason", "参数已存在且未选择覆盖");
                        failureDetails.add(detail);
                        continue;
                    }
                    
                    SystemParameter parameter = existingOpt.orElse(new SystemParameter());
                    if (!existingOpt.isPresent()) {
                        parameter.setId(UUIDUtils.generateUUID());
                        parameter.setParamKey(paramKey);
                        parameter.setCreatedAt(System.currentTimeMillis());
                        parameter.setCreatedBy(getCurrentUser());
                    }
                    
                    parameter.setParamValue((String) paramMap.get("paramValue"));
                    parameter.setParamType((String) paramMap.get("paramType"));
                    parameter.setCategory((String) paramMap.get("category"));
                    parameter.setDescription((String) paramMap.get("description"));
                    parameter.setIsSystem((Boolean) paramMap.getOrDefault("isSystem", false));
                    parameter.setIsEncrypted((Boolean) paramMap.getOrDefault("isEncrypted", false));
                    parameter.setValidationRule((String) paramMap.get("validationRule"));
                    parameter.setMinValue((String) paramMap.get("minValue"));
                    parameter.setMaxValue((String) paramMap.get("maxValue"));
                    parameter.setOptions((String) paramMap.get("options"));
                    parameter.setDefaultValue((String) paramMap.get("defaultValue"));
                    parameter.setRequired((Boolean) paramMap.getOrDefault("required", false));
                    parameter.setVisible((Boolean) paramMap.getOrDefault("visible", true));
                    parameter.setEditable((Boolean) paramMap.getOrDefault("editable", true));
                    parameter.setEffectiveImmediately((Boolean) paramMap.getOrDefault("effectiveImmediately", true));
                    parameter.setRestartRequired((Boolean) paramMap.getOrDefault("restartRequired", false));
                    parameter.setUpdatedAt(System.currentTimeMillis());
                    parameter.setUpdatedBy(getCurrentUser());
                    parameter.setDeleted(0);
                    
                    systemParameterRepository.save(parameter);
                    successCount++;
                    
                } catch (Exception e) {
                    failureCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("paramKey", (String) paramMap.get("paramKey"));
                    detail.put("reason", e.getMessage());
                    failureDetails.add(detail);
                    log.error("导入参数失败：{}", e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("failureDetails", failureDetails);
            
            return result;
            
        } catch (IOException e) {
            log.error("读取导入文件失败", e);
            throw new BusinessException(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("导入参数配置失败", e);
            throw new BusinessException(500, "导入失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResponsePageDataEntity<ParameterHistoryDTO> getParameterHistory(String key, Long startTime, Long endTime, Integer page, Integer size) {
        // TODO: 实现参数历史记录查询
        List<ParameterHistoryDTO> historyList = new ArrayList<>();
        ResponsePageDataEntity<ParameterHistoryDTO> result = new ResponsePageDataEntity<>();
        result.setContent(historyList);
        result.setTotalElements(0L);
        result.setTotalPages(0);
        result.setNumber(page);
        result.setSize(size);
        return result;
    }
    
    @Override
    public Map<String, Object> validateParameter(ParameterValidateDTO validateDTO) {
        try {
            SystemParameter parameter = systemParameterRepository.findByParamKeyAndDeleted(validateDTO.getParamKey(), 0)
                    .orElseThrow(() -> new DataNotFoundException("参数不存在：" + validateDTO.getParamKey()));
            
            validateParameterValue(parameter, validateDTO.getParamValue());
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("errors", new ArrayList<>());
            return result;
            
        } catch (ValidationException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("errors", Collections.singletonList(e.getMessage()));
            return result;
        }
    }
    
    @Override
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public Map<String, Object> refreshParameterCache(String category) {
        int refreshedCount;
        
        if (StringUtils.hasText(category)) {
            refreshedCount = systemParameterRepository.findByCategoryAndDeletedOrderBySortOrderAsc(category, 0).size();
            log.info("刷新参数缓存：category={}, count={}", category, refreshedCount);
        } else {
            refreshedCount = systemParameterRepository.findByVisibleAndDeletedOrderByCategoryAscSortOrderAsc(true, 0).size();
            log.info("刷新全部参数缓存：count={}", refreshedCount);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("refreshedCount", refreshedCount);
        return result;
    }
    
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public String getParameterValue(String key) {
        SystemParameter parameter = systemParameterRepository.findByParamKeyAndDeleted(key, 0)
                .orElse(null);
        
        return parameter != null ? parameter.getParamValue() : null;
    }
    
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public String getParameterValue(String key, String defaultValue) {
        String value = getParameterValue(key);
        return value != null ? value : defaultValue;
    }
    
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public Integer getIntegerParameter(String key, Integer defaultValue) {
        String value = getParameterValue(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("参数值转换为整数失败：key={}, value={}", key, value);
            return defaultValue;
        }
    }
    
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public Boolean getBooleanParameter(String key, Boolean defaultValue) {
        String value = getParameterValue(key);
        if (value == null) {
            return defaultValue;
        }
        
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
    
    /**
     * 将实体转换为DTO
     */
    private SystemParameterDTO convertToDTO(SystemParameter parameter) {
        SystemParameterDTO dto = new SystemParameterDTO();
        dto.setId(parameter.getId());
        dto.setParamKey(parameter.getParamKey());
        
        // 如果是加密参数，进行脱敏处理
        if (Boolean.TRUE.equals(parameter.getIsEncrypted()) && parameter.getParamValue() != null) {
            int length = parameter.getParamValue().length();
            if (length > 6) {
                dto.setParamValue(parameter.getParamValue().substring(0, 3) + "****" + 
                        parameter.getParamValue().substring(length - 3));
            } else {
                dto.setParamValue("******");
            }
        } else {
            dto.setParamValue(parameter.getParamValue());
        }
        
        dto.setParamType(parameter.getParamType());
        dto.setCategory(parameter.getCategory());
        dto.setDescription(parameter.getDescription());
        dto.setIsSystem(parameter.getIsSystem());
        dto.setIsEncrypted(parameter.getIsEncrypted());
        dto.setSortOrder(parameter.getSortOrder());
        dto.setValidationRule(parameter.getValidationRule());
        dto.setMinValue(parameter.getMinValue());
        dto.setMaxValue(parameter.getMaxValue());
        dto.setOptions(parameter.getOptions());
        dto.setDefaultValue(parameter.getDefaultValue());
        dto.setRequired(parameter.getRequired());
        dto.setVisible(parameter.getVisible());
        dto.setEditable(parameter.getEditable());
        dto.setEffectiveImmediately(parameter.getEffectiveImmediately());
        dto.setRestartRequired(parameter.getRestartRequired());
        dto.setCreatedAt(parameter.getCreatedAt());
        dto.setUpdatedAt(parameter.getUpdatedAt());
        dto.setCreatedBy(parameter.getCreatedBy());
        dto.setUpdatedBy(parameter.getUpdatedBy());
        
        return dto;
    }
    
    /**
     * 验证参数值
     */
    private void validateParameterValue(SystemParameter parameter, String value) {
        // 必填验证
        if (Boolean.TRUE.equals(parameter.getRequired()) && !StringUtils.hasText(value)) {
            throw new ValidationException("参数值不能为空");
        }
        
        if (!StringUtils.hasText(value)) {
            return;
        }
        
        // 类型验证
        ParamType paramType = ParamType.fromCode(parameter.getParamType());
        if (paramType != null) {
            switch (paramType) {
                case INTEGER:
                    try {
                        int intValue = Integer.parseInt(value);
                        // 范围验证
                        if (StringUtils.hasText(parameter.getMinValue())) {
                            int min = Integer.parseInt(parameter.getMinValue());
                            if (intValue < min) {
                                throw new ValidationException("参数值不能小于" + min);
                            }
                        }
                        if (StringUtils.hasText(parameter.getMaxValue())) {
                            int max = Integer.parseInt(parameter.getMaxValue());
                            if (intValue > max) {
                                throw new ValidationException("参数值不能大于" + max);
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new ValidationException("参数值必须是整数");
                    }
                    break;
                    
                case DECIMAL:
                    try {
                        double doubleValue = Double.parseDouble(value);
                        // 范围验证
                        if (StringUtils.hasText(parameter.getMinValue())) {
                            double min = Double.parseDouble(parameter.getMinValue());
                            if (doubleValue < min) {
                                throw new ValidationException("参数值不能小于" + min);
                            }
                        }
                        if (StringUtils.hasText(parameter.getMaxValue())) {
                            double max = Double.parseDouble(parameter.getMaxValue());
                            if (doubleValue > max) {
                                throw new ValidationException("参数值不能大于" + max);
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new ValidationException("参数值必须是数字");
                    }
                    break;
                    
                case BOOLEAN:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value) &&
                            !"1".equals(value) && !"0".equals(value)) {
                        throw new ValidationException("参数值必须是布尔值（true/false/1/0）");
                    }
                    break;
                    
                case JSON:
                    try {
                        objectMapper.readTree(value);
                    } catch (Exception e) {
                        throw new ValidationException("参数值必须是有效的JSON格式");
                    }
                    break;
                    
                case ENUM:
                    if (StringUtils.hasText(parameter.getOptions())) {
                        try {
                            List<String> options = objectMapper.readValue(parameter.getOptions(), 
                                    new TypeReference<List<String>>() {});
                            if (!options.contains(value)) {
                                throw new ValidationException("参数值必须是以下选项之一：" + 
                                        String.join(", ", options));
                            }
                        } catch (IOException e) {
                            log.error("解析选项列表失败", e);
                        }
                    }
                    break;
                    
                case DATE:
                    if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        throw new ValidationException("日期格式必须是：YYYY-MM-DD");
                    }
                    break;
                    
                case DATETIME:
                    if (!value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                        throw new ValidationException("日期时间格式必须是：YYYY-MM-DD HH:mm:ss");
                    }
                    break;
                    
                case TIME:
                    if (!value.matches("\\d{2}:\\d{2}:\\d{2}")) {
                        throw new ValidationException("时间格式必须是：HH:mm:ss");
                    }
                    break;
            }
        }
        
        // 正则验证
        if (StringUtils.hasText(parameter.getValidationRule())) {
            if (!Pattern.matches(parameter.getValidationRule(), value)) {
                throw new ValidationException("参数值格式不正确");
            }
        }
    }
    
    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    /**
     * 判断是否是管理员
     */
    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> "ROLE_SUPER_ADMIN".equals(auth.getAuthority()));
    }
}