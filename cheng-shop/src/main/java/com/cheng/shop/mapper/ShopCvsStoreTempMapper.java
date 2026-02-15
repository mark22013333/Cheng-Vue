package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopCvsStoreTemp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 超商門市暫存表 Mapper
 *
 * @author cheng
 */
@Mapper
public interface ShopCvsStoreTempMapper {

    /**
     * 新增門市暫存記錄
     *
     * @param temp 門市暫存資訊
     * @return 影響行數
     */
    int insert(ShopCvsStoreTemp temp);

    /**
     * 根據 storeKey 查詢
     *
     * @param storeKey 前端識別 key
     * @return 門市暫存資訊
     */
    ShopCvsStoreTemp selectByStoreKey(@Param("storeKey") String storeKey);

    /**
     * 根據 storeKey 和 memberId 查詢
     *
     * @param storeKey 前端識別 key
     * @param memberId 會員 ID
     * @return 門市暫存資訊
     */
    ShopCvsStoreTemp selectByStoreKeyAndMemberId(@Param("storeKey") String storeKey,
                                                  @Param("memberId") Long memberId);

    /**
     * 更新門市暫存記錄
     *
     * @param temp 門市暫存資訊
     * @return 影響行數
     */
    int updateByStoreKey(ShopCvsStoreTemp temp);

    /**
     * 根據 storeKey 刪除
     *
     * @param storeKey 前端識別 key
     * @return 影響行數
     */
    int deleteByStoreKey(@Param("storeKey") String storeKey);

    /**
     * 刪除過期記錄
     *
     * @return 刪除行數
     */
    int deleteExpired();
}
