package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.currency.*;
import com.company.financial.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 汇率管理Controller
 * 
 * @author Claude
 * @date 2025-08-25
 */
@RestController
@RequestMapping("/v1/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {
    
    private final ExchangeRateService exchangeRateService;
    
    /**
     * 获取最新汇率
     */
    @GetMapping("/latest")
    public ResponseEntity<ResponseData<BigDecimal>> getLatestRate(
            @RequestParam String fromCurrencyId,
            @RequestParam String toCurrencyId) {
        BigDecimal result = exchangeRateService.getLatestRate(fromCurrencyId, toCurrencyId);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 批量获取当前汇率
     */
    @GetMapping("/current/{baseCurrencyId}")
    public ResponseEntity<ResponseData<List<ExchangeRateHistoryDTO>>> getCurrentRates(
            @PathVariable String baseCurrencyId) {
        List<ExchangeRateHistoryDTO> result = exchangeRateService.getCurrentRates(baseCurrencyId);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 分页查询汇率历史
     */
    @PostMapping("/history")
    public ResponseEntity<ResponseData<ResponsePageDataEntity<ExchangeRateHistoryDTO>>> getRateHistory(
            @RequestBody ExchangeRateQueryDTO queryDTO) {
        ResponsePageDataEntity<ExchangeRateHistoryDTO> result = exchangeRateService.getRateHistory(queryDTO);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 获取指定币种对的汇率历史
     */
    @GetMapping("/history/{fromCurrencyId}/{toCurrencyId}")
    public ResponseEntity<ResponseData<List<ExchangeRateHistoryDTO>>> getRateHistoryByCurrencyPair(
            @PathVariable String fromCurrencyId,
            @PathVariable String toCurrencyId) {
        List<ExchangeRateHistoryDTO> result = exchangeRateService.getRateHistoryByCurrencyPair(fromCurrencyId, toCurrencyId);
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 手动更新汇率
     */
    @PutMapping("/{fromCurrencyId}/{toCurrencyId}")
    public ResponseEntity<ResponseData<Void>> updateRate(
            @PathVariable String fromCurrencyId,
            @PathVariable String toCurrencyId,
            @Valid @RequestBody RateUpdateDTO updateDTO) {
        exchangeRateService.updateRate(fromCurrencyId, toCurrencyId, updateDTO);
        return ResponseEntity.ok(ResponseData.success());
    }
    
    /**
     * 从外部API同步汇率
     */
    @PostMapping("/sync")
    public ResponseEntity<ResponseData<RateSyncDTO>> syncRatesFromExternalAPI() {
        RateSyncDTO result = exchangeRateService.syncRatesFromExternalAPI();
        return ResponseEntity.ok(ResponseData.success(result));
    }
    
    /**
     * 同步指定币种的汇率
     */
    @PostMapping("/sync/{currencyCode}")
    public ResponseEntity<ResponseData<Void>> syncRateForCurrency(@PathVariable String currencyCode) {
        exchangeRateService.syncRateForCurrency(currencyCode);
        return ResponseEntity.ok(ResponseData.success());
    }
    
    /**
     * 获取汇率同步状态
     */
    @GetMapping("/sync/status")
    public ResponseEntity<ResponseData<RateSyncDTO>> getLastSyncStatus() {
        RateSyncDTO result = exchangeRateService.getLastSyncStatus();
        return ResponseEntity.ok(ResponseData.success(result));
    }
}