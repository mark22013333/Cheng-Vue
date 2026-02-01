package com.cheng.shop.service;

import com.cheng.shop.domain.ShopGift;

import java.math.BigDecimal;
import java.util.List;

/**
 * 滿額禮物 Service 介面
 *
 * @author cheng
 */
public interface IShopGiftService {

    /**
     * 查詢禮物列表
     */
    List<ShopGift> selectGiftList(ShopGift gift);

    /**
     * 根據ID查詢禮物
     */
    ShopGift selectGiftById(Long giftId);

    /**
     * 查詢可用禮物（前台：門檻 <= 金額, 庫存 > 0, 狀態啟用）
     */
    List<ShopGift> selectAvailableGifts(BigDecimal amount);

    /**
     * 新增禮物
     */
    int insertGift(ShopGift gift);

    /**
     * 更新禮物
     */
    int updateGift(ShopGift gift);

    /**
     * 刪除禮物
     */
    int deleteGiftById(Long giftId);

    /**
     * 批量刪除禮物
     */
    int deleteGiftByIds(Long[] giftIds);

    /**
     * 扣減禮物庫存（出貨時呼叫）
     */
    int decreaseStock(Long giftId, int quantity);
}
