package com.cheng.shop.service;

import com.cheng.shop.domain.ShopBanner;

import java.util.List;

/**
 * 輪播 Service 介面
 *
 * @author cheng
 */
public interface IShopBannerService {

    /**
     * 查詢輪播列表
     *
     * @param banner 查詢條件
     * @return 輪播列表
     */
    List<ShopBanner> selectBannerList(ShopBanner banner);

    /**
     * 根據ID查詢輪播
     *
     * @param bannerId 輪播ID
     * @return 輪播
     */
    ShopBanner selectBannerById(Long bannerId);

    /**
     * 查詢有效的輪播（前台用）
     *
     * @param position 展示位置
     * @return 輪播列表
     */
    List<ShopBanner> selectActiveBanners(String position);

    /**
     * 新增輪播
     *
     * @param banner 輪播
     * @return 影響行數
     */
    int insertBanner(ShopBanner banner);

    /**
     * 更新輪播
     *
     * @param banner 輪播
     * @return 影響行數
     */
    int updateBanner(ShopBanner banner);

    /**
     * 刪除輪播
     *
     * @param bannerId 輪播ID
     * @return 影響行數
     */
    int deleteBannerById(Long bannerId);

    /**
     * 批量刪除輪播
     *
     * @param bannerIds 輪播ID陣列
     * @return 影響行數
     */
    int deleteBannerByIds(Long[] bannerIds);
}
