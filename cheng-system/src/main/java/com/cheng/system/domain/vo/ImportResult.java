package com.cheng.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 匯入結果封裝類
 *
 * @author cheng
 */
@Data
public class ImportResult {

    /**
     * 資料統計
     */
    private int totalRows;           // 總行數
    private int successRows;         // 成功行數
    private int failedRows;          // 失敗行數
    private int skippedRows;         // 跳過行數（重複且未選擇更新）

    /**
     * 圖片統計
     */
    private int totalImages;         // 總圖片數
    private int copiedImages;        // 成功複製的圖片數
    private int overwrittenImages;   // 覆蓋的圖片數
    private int missingImages;       // 缺失的圖片數

    /**
     * 詳細錯誤列表
     */
    private List<RowError> rowErrors = new ArrayList<>();

    /**
     * 單行錯誤資訊
     */
    @Data
    public static class RowError {
        private int rowNum;          // 行號（Excel 中的行號）
        private String itemName;     // 物品名稱
        private String errorMessage; // 錯誤訊息

        public RowError(int rowNum, String itemName, String errorMessage) {
            this.rowNum = rowNum;
            this.itemName = itemName;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 新增錯誤
     */
    public void addError(int rowNum, String itemName, String errorMessage) {
        rowErrors.add(new RowError(rowNum, itemName, errorMessage));
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failedRows == 0 && skippedRows == 0 && missingImages == 0;
    }

    /**
     * 是否有警告（部分成功）
     */
    public boolean hasWarnings() {
        return !isAllSuccess() && successRows > 0;
    }

    /**
     * 格式化為 HTML 訊息
     */
    public String toHtmlMessage() {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-size: 14px; line-height: 1.6;'>");

        // 總結
        if (isAllSuccess()) {
            html.append("<div style='margin-bottom: 10px; color: #67C23A; font-weight: bold;'>");
            html.append("✅ 匯入成功");
            html.append("</div>");
            html.append("<div style='margin-bottom: 10px;'>");
            html.append("成功匯入 <strong>").append(successRows).append("</strong> 筆資料");
            if (copiedImages > 0) {
                html.append("，複製 <strong>").append(copiedImages).append("</strong> 張圖片");
            }
            html.append("</div>");
        } else if (hasWarnings()) {
            html.append("<div style='margin-bottom: 10px; color: #E6A23C; font-weight: bold;'>");
            html.append("⚠️ 部分成功");
            html.append("</div>");
            html.append("<div style='margin-bottom: 10px;'>");
            html.append("成功 <strong>").append(successRows).append("</strong> 筆");
            if (failedRows > 0) {
                html.append("，失敗 <strong style='color: #F56C6C;'>").append(failedRows).append("</strong> 筆");
            }
            if (skippedRows > 0) {
                html.append("，跳過 <strong>").append(skippedRows).append("</strong> 筆");
            }
            html.append("</div>");
        } else {
            html.append("<div style='margin-bottom: 10px; color: #F56C6C; font-weight: bold;'>");
            html.append("❌ 匯入失敗");
            html.append("</div>");
            html.append("<div style='margin-bottom: 10px;'>");
            html.append("失敗 <strong>").append(failedRows).append("</strong> 筆");
            if (skippedRows > 0) {
                html.append("，跳過 <strong>").append(skippedRows).append("</strong> 筆");
            }
            html.append("</div>");
        }

        // 圖片統計
        if (totalImages > 0) {
            html.append("<div style='margin: 10px 0; padding: 8px; background: #f5f7fa; border-radius: 4px;'>");
            html.append("📷 圖片：複製 <strong>").append(copiedImages).append("</strong>");
            if (overwrittenImages > 0) {
                html.append("，覆蓋 <strong>").append(overwrittenImages).append("</strong>");
            }
            if (missingImages > 0) {
                html.append("，<strong style='color: #E6A23C;'>缺失 ").append(missingImages).append("</strong>");
            }
            html.append("</div>");
        }

        // 錯誤詳情
        if (!rowErrors.isEmpty()) {
            html.append("<div style='margin-top: 15px;'>");
            html.append("<div style='font-weight: bold; margin-bottom: 8px;'>錯誤詳情：</div>");

            int maxErrors = 10;  // 最多顯示 10 個錯誤
            for (int i = 0; i < Math.min(rowErrors.size(), maxErrors); i++) {
                RowError error = rowErrors.get(i);
                html.append("<div style='margin-left: 20px; margin-bottom: 5px;'>");
                html.append("• 第 <code>").append(error.getRowNum()).append("</code> 行");
                if (error.getItemName() != null && !error.getItemName().isEmpty()) {
                    html.append("（<strong>").append(error.getItemName()).append("</strong>）");
                }
                html.append("：<span style='color: #F56C6C;'>").append(error.getErrorMessage()).append("</span>");
                html.append("</div>");
            }

            if (rowErrors.size() > maxErrors) {
                html.append("<div style='margin-left: 20px; color: #909399;'>");
                html.append("... 還有 ").append(rowErrors.size() - maxErrors).append(" 個錯誤未顯示");
                html.append("</div>");
            }
            html.append("</div>");
        }

        html.append("</div>");
        return html.toString();
    }
}
