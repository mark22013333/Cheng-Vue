package com.cheng.crawler.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 公司重大訊息 DTO
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Data
@Accessors(chain = true)
public class CompanyNewsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公司代號
     */
    private String companyId;

    /**
     * 公司名稱
     */
    private String companyName;

    /**
     * 市場名稱
     */
    private String marketName;

    /**
     * 主旨
     */
    private String subject;

    /**
     * 日期
     */
    private String date;

    /**
     * 時間
     */
    private String time;

    /**
     * 發布日期(格式化後)
     */
    private String publishDate;

    /**
     * 序號
     */
    private String serialNumber;

    /**
     * 發言人
     */
    private String speaker;

    /**
     * 發言人職稱
     */
    private String speakerTitle;

    /**
     * 發言人電話
     */
    private String speakerPhone;

    /**
     * 符合條款
     */
    private String matchItem;

    /**
     * 事實發生日
     */
    private String eventDate;

    /**
     * 說明
     */
    private String description;

    /**
     * HTML 內容
     */
    private String content;

    /**
     * API 參數
     */
    private Map<String, String> parameters;
}
