package com.cheng.crawler.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬蟲匯入記錄
 *
 * @author cheng
 */
@Data
public class CrawlImportLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long logId;
    private String batchId;
    private String barcode;
    private String productName;
    private String status;
    private Long productId;
    private String detailUrl;
    private String errorMessage;
    private LocalDateTime createTime;

    /** 狀態常數 */
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";
    public static final String STATUS_MULTIPLE = "MULTIPLE";
    public static final String STATUS_IMG_FAIL = "IMG_FAIL";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SKIPPED = "SKIPPED";

    public static CrawlImportLog of(String batchId, String barcode, String productName, String status) {
        CrawlImportLog log = new CrawlImportLog();
        log.setBatchId(batchId);
        log.setBarcode(barcode);
        log.setProductName(productName);
        log.setStatus(status);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }
}
