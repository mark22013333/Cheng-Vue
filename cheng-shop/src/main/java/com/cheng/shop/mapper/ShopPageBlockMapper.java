package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopPageBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 頁面區塊 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopPageBlockMapper {

    /**
     * 查詢區塊列表
     *
     * @param block 查詢條件
     * @return 區塊列表
     */
    List<ShopPageBlock> selectBlockList(ShopPageBlock block);

    /**
     * 根據ID查詢區塊
     *
     * @param blockId 區塊ID
     * @return 區塊
     */
    ShopPageBlock selectBlockById(Long blockId);

    /**
     * 根據頁面和區塊識別查詢
     *
     * @param pageKey 頁面識別
     * @param blockKey 區塊識別
     * @return 區塊
     */
    ShopPageBlock selectBlockByPageAndKey(@Param("pageKey") String pageKey, @Param("blockKey") String blockKey);

    /**
     * 新增區塊
     *
     * @param block 區塊
     * @return 影響行數
     */
    int insertBlock(ShopPageBlock block);

    /**
     * 更新區塊
     *
     * @param block 區塊
     * @return 影響行數
     */
    int updateBlock(ShopPageBlock block);

    /**
     * 刪除區塊
     *
     * @param blockId 區塊ID
     * @return 影響行數
     */
    int deleteBlockById(Long blockId);

    /**
     * 檢查區塊識別是否唯一
     *
     * @param pageKey 頁面識別
     * @param blockKey 區塊識別
     * @param blockId   排除的區塊ID
     * @return 數量
     */
    int checkBlockKeyUnique(@Param("pageKey") String pageKey, @Param("blockKey") String blockKey, @Param("blockId") Long blockId);
}
