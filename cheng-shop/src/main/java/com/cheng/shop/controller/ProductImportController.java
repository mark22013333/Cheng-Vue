package com.cheng.shop.controller;

import com.cheng.common.core.domain.AjaxResult;
import com.cheng.shop.service.impl.ProductImportServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品匯入 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportServiceImpl productImportService;

    /**
     * 從 CSV 匯入商品
     *
     * @param csvPath CSV 檔案路徑
     * @return 匯入結果摘要
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @PostMapping("/product-import")
    public AjaxResult importProducts(@RequestParam String csvPath) {
        log.info("收到商品匯入請求，CSV 路徑: {}", csvPath);
        String result = productImportService.executeImport(csvPath);
        return AjaxResult.success(result);
    }
}
