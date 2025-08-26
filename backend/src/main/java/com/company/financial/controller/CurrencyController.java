package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.currency.*;
import com.company.financial.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 币种管理Controller
 * 
 * @author Claude
 * @date 2025-08-25
 */
@RestController
@RequestMapping("/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    
    private final CurrencyService currencyService;
    
    /**
     * 分页查询币种列表
     */
    @PostMapping("/list")
    public ResponseEntity<ResponseData<ResponsePageDataEntity<CurrencyDTO>>> getCurrencyList(
            @RequestBody CurrencyQueryDTO queryDTO) {
        ResponsePageDataEntity<CurrencyDTO> result = currencyService.getCurrencyList(queryDTO);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 获取所有币种下拉列表
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseData<List<CurrencyDTO>>> getAllCurrencies() {
        List<CurrencyDTO> result = currencyService.getAllCurrencies();
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 根据ID获取币种详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<CurrencyDTO>> getCurrencyById(@PathVariable String id) {
        CurrencyDTO result = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 根据代码获取币种详情
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseData<CurrencyDTO>> getCurrencyByCode(@PathVariable String code) {
        CurrencyDTO result = currencyService.getCurrencyByCode(code);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 创建币种
     */
    @PostMapping
    public ResponseEntity<ResponseData<CurrencyDTO>> createCurrency(@Valid @RequestBody CurrencyCreateDTO createDTO) {
        CurrencyDTO result = currencyService.createCurrency(createDTO);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 更新币种
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<CurrencyDTO>> updateCurrency(
            @PathVariable String id, 
            @Valid @RequestBody CurrencyUpdateDTO updateDTO) {
        CurrencyDTO result = currencyService.updateCurrency(id, updateDTO);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 启用/禁用币种
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ResponseData<Void>> toggleCurrencyStatus(
            @PathVariable String id, 
            @PathVariable Integer status) {
        currencyService.toggleCurrencyStatus(id, status);
        return ResponseEntity.ok(ResponseData.success());
    }
    
    /**
     * 设置基准币种
     */
    @PutMapping("/{id}/set-base")
    public ResponseEntity<ResponseData<Void>> setBaseCurrency(@PathVariable String id) {
        currencyService.setBaseCurrency(id);
        return ResponseEntity.ok(ResponseData.success());
    }
    
    /**
     * 删除币种
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteCurrency(@PathVariable String id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.ok(ResponseData.success());
    }
    
    /**
     * 获取基准币种
     */
    @GetMapping("/base")
    public ResponseEntity<ResponseData<CurrencyDTO>> getBaseCurrency() {
        CurrencyDTO result = currencyService.getBaseCurrency();
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 检查币种代码是否存在
     */
    @GetMapping("/exists/{code}")
    public ResponseEntity<ResponseData<Boolean>> existsByCode(@PathVariable String code) {
        boolean result = currencyService.existsByCode(code);
        return ResponseEntity.ok(ResponseData.success(result));
    }
}