package com.cheng.shop.service;

import com.cheng.shop.domain.ShopCart;

import java.util.List;

/**
 * 購物車 Service 介面
 *
 * @author cheng
 */
public interface IShopCartService {

    /**
     * 查詢會員購物車列表
     *
     * @param memberId 會員ID
     * @return 購物車列表
     */
    List<ShopCart> selectCartListByMemberId(Long memberId);

    /**
     * 查詢已選中的購物車項目
     *
     * @param memberId 會員ID
     * @return 購物車列表
     */
    List<ShopCart> selectSelectedCartsByMemberId(Long memberId);

    /**
     * 加入購物車
     *
     * @param memberId 會員ID
     * @param skuId    SKU ID
     * @param quantity 數量
     * @return 影響行數
     */
    int addToCart(Long memberId, Long skuId, int quantity);

    /**
     * 更新購物車數量
     *
     * @param cartId   購物車ID
     * @param quantity 數量
     * @return 影響行數
     */
    int updateCartQuantity(Long cartId, int quantity);

    /**
     * 更新選中狀態
     *
     * @param cartId   購物車ID
     * @param selected 是否選中
     * @return 影響行數
     */
    int updateCartSelected(Long cartId, boolean selected);

    /**
     * 全選/取消全選
     *
     * @param memberId 會員ID
     * @param selected 是否選中
     * @return 影響行數
     */
    int updateAllSelected(Long memberId, boolean selected);

    /**
     * 刪除購物車項目
     *
     * @param cartId 購物車ID
     * @return 影響行數
     */
    int deleteCartById(Long cartId);

    /**
     * 批量刪除購物車項目
     *
     * @param cartIds 購物車ID陣列
     * @return 影響行數
     */
    int deleteCartByIds(Long[] cartIds);

    /**
     * 清空會員購物車
     *
     * @param memberId 會員ID
     * @return 影響行數
     */
    int clearCart(Long memberId);

    /**
     * 刪除已選中的購物車項目
     *
     * @param memberId 會員ID
     * @return 影響行數
     */
    int deleteSelectedCarts(Long memberId);

    /**
     * 統計會員購物車數量
     *
     * @param memberId 會員ID
     * @return 數量
     */
    int countCartByMemberId(Long memberId);
}
