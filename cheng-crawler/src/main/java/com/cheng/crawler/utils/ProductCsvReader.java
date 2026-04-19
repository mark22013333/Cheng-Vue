package com.cheng.crawler.utils;

import com.cheng.crawler.dto.ProductCsvRow;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Sheets CSV 讀取器
 * 欄位順序：狀態, 條碼編號, 品名, 規格, 品號, 定價一, 零售價, 九折價
 *
 * @author cheng
 */
@Slf4j
public class ProductCsvReader {

    private ProductCsvReader() {
    }

    /**
     * 讀取 CSV 檔案，僅保留「正常品」
     *
     * @param csvPath CSV 檔案路徑
     * @return 商品列表
     */
    public static List<ProductCsvRow> read(String csvPath) throws IOException {
        Path path = Path.of(csvPath);
        if (!Files.exists(path)) {
            throw new IOException("CSV 檔案不存在: " + csvPath);
        }

        List<ProductCsvRow> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV 檔案為空");
            }
            log.info("CSV 表頭: {}", headerLine);

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 8) {
                        log.warn("第 {} 行欄位不足（{}欄），跳過: {}", lineNum, fields.length, line);
                        continue;
                    }

                    String status = fields[0].trim();
                    if (!"正常品".equals(status)) {
                        continue;
                    }

                    String barcode = fields[1].trim();
                    String productName = fields[2].trim();
                    if (barcode.isEmpty() || productName.isEmpty()) {
                        log.warn("第 {} 行缺少條碼或品名，跳過", lineNum);
                        continue;
                    }

                    ProductCsvRow row = new ProductCsvRow();
                    row.setBarcode(barcode);
                    row.setProductName(productName);
                    row.setSpec(fields[3].trim());
                    row.setProductCode(fields[4].trim());
                    row.setCostPrice(parsePrice(fields[5].trim()));
                    row.setRetailPrice(parsePrice(fields[6].trim()));
                    row.setDiscountPrice(parsePrice(fields[7].trim()));
                    rows.add(row);
                } catch (Exception e) {
                    log.warn("第 {} 行解析失敗: {}", lineNum, e.getMessage());
                }
            }
        }
        log.info("CSV 讀取完成，共 {} 筆正常品", rows.size());
        return rows;
    }

    /**
     * 解析 CSV 行（處理引號包圍的欄位）
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * 解析價格（處理含逗號的格式如 "1,280.00"）
     */
    private static BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cleaned = priceStr.replace(",", "").replace("\"", "").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("價格解析失敗: {}", priceStr);
            return BigDecimal.ZERO;
        }
    }
}
