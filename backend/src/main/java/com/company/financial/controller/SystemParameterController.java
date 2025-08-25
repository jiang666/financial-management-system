package com.company.financial.controller;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.common.response.ResponseData;
import com.company.financial.dto.parameter.*;
import com.company.financial.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 系统参数Controller
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/parameters")
@RequiredArgsConstructor
@Validated
public class SystemParameterController {
    
    private final SystemParameterService systemParameterService;
    
    /**
     * 获取参数列表
     */
    @GetMapping
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_VIEW')")
    public ResponseData<ResponsePageDataEntity<SystemParameterDTO>> getParameterList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        SystemParameterQueryDTO queryDTO = new SystemParameterQueryDTO();
        queryDTO.setCategory(category);
        queryDTO.setKeyword(keyword);
        queryDTO.setIsSystem(isSystem);
        queryDTO.setVisible(visible);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        
        ResponsePageDataEntity<SystemParameterDTO> pageData = systemParameterService.getParameterList(queryDTO);
        return ResponseData.success(pageData);
    }
    
    /**
     * 获取单个参数
     */
    @GetMapping("/{key}")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_VIEW')")
    public ResponseData<SystemParameterDTO> getParameter(@PathVariable String key) {
        SystemParameterDTO parameter = systemParameterService.getParameterByKey(key);
        return ResponseData.success(parameter);
    }
    
    /**
     * 更新参数
     */
    @PutMapping("/{key}")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_EDIT')")
    public ResponseData<Void> updateParameter(
            @PathVariable String key,
            @Valid @RequestBody SystemParameterUpdateDTO updateDTO) {
        
        systemParameterService.updateParameter(key, updateDTO);
        return ResponseData.success();
    }
    
    /**
     * 批量更新参数
     */
    @PostMapping("/batch")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_EDIT')")
    public ResponseData<Map<String, Object>> batchUpdateParameters(
            @Valid @RequestBody SystemParameterBatchUpdateDTO batchUpdateDTO) {
        
        Map<String, Object> result = systemParameterService.batchUpdateParameters(batchUpdateDTO);
        return ResponseData.success("批量更新成功", result);
    }
    
    /**
     * 获取参数分类
     */
    @GetMapping("/categories")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_VIEW')")
    public ResponseData<List<ParameterCategoryDTO>> getParameterCategories() {
        List<ParameterCategoryDTO> categories = systemParameterService.getParameterCategories();
        return ResponseData.success(categories);
    }
    
    /**
     * 导出参数配置
     */
    @GetMapping("/export")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_EXPORT')")
    public void exportParameters(
            @RequestParam(required = false) String category,
            HttpServletResponse response) {
        
        systemParameterService.exportParameters(category, response);
    }
    
    /**
     * 导入参数配置
     */
    @PostMapping("/import")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_IMPORT')")
    public ResponseData<Map<String, Object>> importParameters(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") Boolean overwrite) {
        
        Map<String, Object> result = systemParameterService.importParameters(file, overwrite);
        return ResponseData.success("导入成功", result);
    }
    
    /**
     * 获取参数修改历史
     */
    @GetMapping("/{key}/history")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_HISTORY')")
    public ResponseData<ResponsePageDataEntity<ParameterHistoryDTO>> getParameterHistory(
            @PathVariable String key,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        ResponsePageDataEntity<ParameterHistoryDTO> pageData = 
                systemParameterService.getParameterHistory(key, startTime, endTime, page, size);
        return ResponseData.success(pageData);
    }
    
    /**
     * 验证参数值
     */
    @PostMapping("/validate")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_VIEW')")
    public ResponseData<Map<String, Object>> validateParameter(
            @Valid @RequestBody ParameterValidateDTO validateDTO) {
        
        Map<String, Object> result = systemParameterService.validateParameter(validateDTO);
        return ResponseData.success("验证完成", result);
    }
    
    /**
     * 刷新参数缓存
     */
    @PostMapping("/refresh")
    // @PreAuthorize("hasAuthority('SYSTEM_PARAM_ADMIN')")
    public ResponseData<Map<String, Object>> refreshParameterCache(
            @RequestParam(required = false) String category) {
        
        Map<String, Object> result = systemParameterService.refreshParameterCache(category);
        return ResponseData.success("缓存刷新成功", result);
    }
}