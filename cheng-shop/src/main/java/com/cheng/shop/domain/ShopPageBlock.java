package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 頁面區塊實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopPageBlock extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 區塊ID
     */
    private Long blockId;

    /**
     * 頁面識別：HOME/ABOUT/CONTACT
     */
    private String pageKey;

    /**
     * 區塊識別
     */
    private String blockKey;

    /**
     * 區塊標題
     */
    private String title;

    /**
     * 區塊副標題
     */
    private String subTitle;

    /**
     * 區塊圖片
     */
    private String imageUrl;

    /**
     * 額外配置（JSON）
     */
    private String extraConfig;

    /**
     * 區塊類型：TEXT/IMAGE/HTML/PRODUCT_LIST
     */
    private String blockType;

    /**
     * 內容（JSON格式）
     */
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 狀態：1啟用 0停用
     */
    private String status;
}
