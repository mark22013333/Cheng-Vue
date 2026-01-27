package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.shop.enums.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分類實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 父分類ID（0表示一級分類）
     */
    private Long parentId;

    /**
     * 分類名稱
     */
    private String name;

    /**
     * 分類圖示
     */
    private String icon;

    /**
     * 分類橫幅圖片
     */
    private String bannerImage;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 狀態
     */
    private String status;

    /**
     * 子分類
     */
    private List<ShopCategory> children = new ArrayList<>();

    /**
     * 取得狀態列舉
     */
    public CommonStatus getStatusEnum() {
        return status != null ? CommonStatus.fromCode(status) : null;
    }

    /**
     * 設定狀態列舉
     */
    public void setStatusEnum(CommonStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
}
