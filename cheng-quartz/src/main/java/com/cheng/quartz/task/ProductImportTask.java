package com.cheng.quartz.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 商品匯入定時任務
 * <p>
 * 透過 Quartz 後台觸發 CSV 商品匯入
 * <p>
 * <b>Quartz 設定（預設，內建 classpath 檔案）：</b>
 * <pre>
 * 調用目標字串: productImportTask.run('classpath:import/prostaff-products.csv')
 * </pre>
 * <b>Quartz 設定（相對檔名，需設定 cheng.import.base-path）：</b>
 * <pre>
 * 調用目標字串: productImportTask.run('prostaff-products.csv')
 * </pre>
 *
 * @author cheng
 */
@Slf4j
@Component("productImportTask")
public class ProductImportTask {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 執行商品匯入
     *
     * @param param CSV 檔案路徑或包含 csvPath 的 JSON 字串
     */
    public void run(String param) {
        String csvPath = extractCsvPath(param);
        log.info("定時任務觸發商品匯入，CSV: {}", csvPath);

        try {
            Object importService = applicationContext.getBean("productImportServiceImpl");
            Method executeMethod = importService.getClass().getMethod("executeImport", String.class);
            String result = (String) executeMethod.invoke(importService, csvPath);
            log.info("商品匯入結果: {}", result);
        } catch (Exception e) {
            log.error("商品匯入任務執行失敗", e);
        }
    }

    /**
     * 從參數中提取 CSV 路徑
     * 支援兩種格式：純路徑字串 或 JSON（{"csvPath":"..."}）
     */
    private String extractCsvPath(String param) {
        if (param == null || param.isBlank()) {
            throw new IllegalArgumentException("CSV 路徑參數不得為空");
        }

        String trimmed = param.trim();
        if (trimmed.startsWith("{")) {
            JSONObject json = JSON.parseObject(trimmed);
            String path = json.getString("csvPath");
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("JSON 參數中缺少 csvPath 欄位: " + trimmed);
            }
            return path;
        }

        return trimmed;
    }
}
