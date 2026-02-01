package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopGift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 滿額禮物 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopGiftMapper {

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
    List<ShopGift> selectAvailableGifts(@Param("amount") BigDecimal amount);

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
     * 扣減禮物庫存
     */
    int decreaseStock(@Param("giftId") Long giftId, @Param("quantity") int quantity);
}
