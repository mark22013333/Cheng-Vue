package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 購物車 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopCartMapper {

    /**
     * 查詢會員購物車列表
     *
     * @param memberId 會員ID
     * @return 購物車列表
     */
    List<ShopCart> selectCartListByMemberId(Long memberId);

    /**
     * 根據ID查詢購物車項目
     *
     * @param cartId 購物車ID
     * @return 購物車項目
     */
    ShopCart selectCartById(Long cartId);

    /**
     * 查詢會員的指定SKU購物車項目
     *
     * @param memberId 會員ID
     * @param skuId    SKU ID
     * @return 購物車項目
     */
    ShopCart selectCartByMemberAndSku(@Param("memberId") Long memberId, @Param("skuId") Long skuId);

    /**
     * 查詢已選中的購物車項目
     *
     * @param memberId 會員ID
     * @return 購物車列表
     */
    List<ShopCart> selectSelectedCartsByMemberId(Long memberId);

    /**
     * 新增購物車項目
     *
     * @param cart 購物車
     * @return 影響行數
     */
    int insertCart(ShopCart cart);

    /**
     * 更新購物車項目
     *
     * @param cart 購物車
     * @return 影響行數
     */
    int updateCart(ShopCart cart);

    /**
     * 更新購物車數量
     *
     * @param cartId   購物車ID
     * @param quantity 數量
     * @return 影響行數
     */
    int updateCartQuantity(@Param("cartId") Long cartId, @Param("quantity") int quantity);

    /**
     * 更新選中狀態
     *
     * @param cartId   購物車ID
     * @param selected 是否選中
     * @return 影響行數
     */
    int updateCartSelected(@Param("cartId") Long cartId, @Param("isSelected") boolean isSelected);

    /**
     * 全選/取消全選
     *
     * @param memberId 會員ID
     * @param selected 是否選中
     * @return 影響行數
     */
    int updateAllSelected(@Param("memberId") Long memberId, @Param("isSelected") boolean isSelected);

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
    int deleteCartByMemberId(Long memberId);

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
