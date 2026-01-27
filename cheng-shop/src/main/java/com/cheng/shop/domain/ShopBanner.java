package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.shop.enums.BannerLinkType;
import com.cheng.shop.enums.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 首頁輪播實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopBanner extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 輪播ID
     */
    private Long bannerId;

    /**
     * 標題
     */
    private String title;

    /**
     * 圖片URL
     */
    private String imageUrl;

    /**
     * 手機版圖片URL
     */
    private String mobileImage;

    /**
     * 連結類型
     */
    private String linkType;

    /**
     * 連結值
     */
    private String linkValue;

    /**
     * 展示位置
     */
    private String position;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 開始時間
     */
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    private LocalDateTime endTime;

    /**
     * 狀態
     */
    private String status;

    // ============ Enum Getter/Setter ============

    public CommonStatus getStatusEnum() {
        return status != null ? CommonStatus.fromCode(status) : null;
    }

    public void setStatusEnum(CommonStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }

    public BannerLinkType getLinkTypeEnum() {
        return linkType != null ? BannerLinkType.fromCode(linkType) : null;
    }

    public void setLinkTypeEnum(BannerLinkType linkTypeEnum) {
        this.linkType = linkTypeEnum != null ? linkTypeEnum.getCode() : null;
    }
}
