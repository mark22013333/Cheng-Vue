package com.cheng.crawler.utils;

import com.cheng.crawler.dto.ProductCsvRow;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Sheets CSV 讀取器
 * 欄位順序：狀態, 條碼編號, 品名, 規格, 品號, 定價一, 零售價, 九折價
 *
 * <p>來源（source）支援兩種格式：</p>
 * <ul>
 *     <li><b>HTTP(S) URL</b>：從遠端下載（建議使用 Google Sheets 發布 CSV 連結，
 *         例：{@code https://docs.google.com/spreadsheets/d/xxx/export?format=csv}）</li>
 *     <li><b>相對檔名</b>：組合 {@code basePath}（由設定檔 {@code cheng.import.base-path}
 *         提供）讀取本機檔案。禁止絕對路徑與 {@code ..} 路徑穿越。</li>
 * </ul>
 *
 * @author cheng
 */
@Slf4j
public class ProductCsvReader {

    private static final long HTTP_CONNECT_TIMEOUT_SECONDS = 10L;
    private static final long HTTP_READ_TIMEOUT_SECONDS = 30L;

    /** 比對 Google Sheets URL 並取出試算表 ID 與後續路徑。 */
    private static final Pattern GOOGLE_SHEETS_URL =
            Pattern.compile("^https?://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9_-]+)(.*)$");

    /** 從 Google Sheets URL 片段中抓出 gid。 */
    private static final Pattern GID_PATTERN = Pattern.compile("gid=(\\d+)");

    private ProductCsvReader() {
    }

    /**
     * 讀取 CSV 來源，僅保留「正常品」。
     *
     * @param source   CSV 來源：HTTP(S) URL 或相對檔名
     * @param basePath 相對檔名時使用的基準目錄（來自 {@code cheng.import.base-path}）
     * @return 商品列表
     * @throws IOException 來源不存在、下載失敗或內容為空
     */
    public static List<ProductCsvRow> read(String source, String basePath) throws IOException {
        if (source == null || source.isBlank()) {
            throw new IOException("CSV 來源不可為空");
        }
        String trimmed = source.trim();

        if (isHttpUrl(trimmed)) {
            String downloadUrl = normalizeGoogleSheetsUrl(trimmed);
            if (!downloadUrl.equals(trimmed)) {
                log.info("Google Sheets URL 已自動正規化為 CSV 匯出連結: {}", downloadUrl);
            }
            log.info("由 URL 讀取 CSV: {}", downloadUrl);
            String content = downloadFromUrl(downloadUrl);
            try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
                return parseCsv(reader);
            }
        }

        Path path = resolveLocalPath(trimmed, basePath);
        log.info("由本機檔案讀取 CSV: {}", path);
        if (!Files.exists(path)) {
            throw new IOException("CSV 檔案不存在: " + path);
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parseCsv(reader);
        }
    }

    private static boolean isHttpUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    /**
     * 將 Google Sheets 編輯 URL 轉換為 CSV 匯出 URL。
     *
     * <p>支援輸入：</p>
     * <ul>
     *     <li>{@code /edit?gid=0#gid=0}、{@code /edit}、{@code /view} 等</li>
     *     <li>已是 {@code /export?format=csv&gid=0} 則直接原樣返回</li>
     *     <li>非 Google Sheets URL 原樣返回</li>
     * </ul>
     */
    static String normalizeGoogleSheetsUrl(String url) {
        if (url.contains("/export") && url.contains("format=csv")) {
            return url;
        }
        Matcher m = GOOGLE_SHEETS_URL.matcher(url);
        if (!m.matches()) {
            return url;
        }
        String sheetId = m.group(1);
        String rest = m.group(2);
        String gid = "0";
        Matcher gidMatcher = GID_PATTERN.matcher(rest);
        if (gidMatcher.find()) {
            gid = gidMatcher.group(1);
        }
        return "https://docs.google.com/spreadsheets/d/" + sheetId + "/export?format=csv&gid=" + gid;
    }

    /**
     * 將相對檔名組合成安全的本機路徑。禁止絕對路徑與跨目錄存取。
     */
    private static Path resolveLocalPath(String source, String basePath) throws IOException {
        if (basePath == null || basePath.isBlank()) {
            throw new IOException("未設定 cheng.import.base-path，無法解析相對檔名: " + source);
        }
        Path base = Path.of(basePath).toAbsolutePath().normalize();
        Path candidate = Path.of(source);
        if (candidate.isAbsolute()) {
            throw new IOException("禁止使用絕對路徑，請改用相對檔名或 HTTP(S) URL: " + source);
        }
        Path resolved = base.resolve(candidate).normalize();
        if (!resolved.startsWith(base)) {
            throw new IOException("禁止跨目錄存取（含 ..）: " + source);
        }
        return resolved;
    }

    private static String downloadFromUrl(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HTTP_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "text/csv, text/plain, */*")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("CSV 下載失敗，HTTP " + response.code() + ": " + url);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("CSV 下載失敗，回應內容為空: " + url);
            }
            return body.string();
        }
    }

    /**
     * 共用 CSV 解析邏輯
     */
    private static List<ProductCsvRow> parseCsv(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new IOException("CSV 內容為空");
        }
        log.info("CSV 表頭: {}", headerLine);

        List<ProductCsvRow> rows = new ArrayList<>();
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
