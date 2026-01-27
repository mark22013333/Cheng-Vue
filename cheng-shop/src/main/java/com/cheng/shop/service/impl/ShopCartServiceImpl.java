package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.DateUtils;
import com.cheng.shop.domain.ShopCart;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.mapper.ShopCartMapper;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import com.cheng.shop.service.IShopCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 購物車 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCartServiceImpl implements IShopCartService {

    private final ShopCartMapper cartMapper;
    private final ShopProductSkuMapper skuMapper;

    @Override
    public List<ShopCart> selectCartListByMemberId(Long memberId) {
        return cartMapper.selectCartListByMemberId(memberId);
    }

    @Override
    public List<ShopCart> selectSelectedCartsByMemberId(Long memberId) {
        return cartMapper.selectSelectedCartsByMemberId(memberId);
    }

    @Override
    public int addToCart(Long memberId, Long skuId, int quantity) {
        // 檢查 SKU 是否存在
        ShopProductSku sku = skuMapper.selectSkuById(skuId);
        if (sku == null) {
            throw new ServiceException("商品規格不存在");
        }

        // 檢查庫存
        if (sku.getStockQuantity() < quantity) {
            throw new ServiceException("商品庫存不足");
        }

        // 檢查購物車是否已有此 SKU
        ShopCart existingCart = cartMapper.selectCartByMemberAndSku(memberId, skuId);
        if (existingCart != null) {
            // 更新數量
            int newQuantity = existingCart.getQuantity() + quantity;
            if (sku.getStockQuantity() < newQuantity) {
                throw new ServiceException("商品庫存不足");
            }
            return cartMapper.updateCartQuantity(existingCart.getCartId(), newQuantity);
        }

        // 新增購物車項目
        ShopCart cart = new ShopCart();
        cart.setMemberId(memberId);
        cart.setSkuId(skuId);
        cart.setQuantity(quantity);
        cart.setIsSelected(true);
        cart.setCreateTime(DateUtils.getNowDate());

        log.info("加入購物車，會員ID: {}, SKU: {}, 數量: {}", memberId, skuId, quantity);
        return cartMapper.insertCart(cart);
    }

    @Override
    public int updateCartQuantity(Long cartId, int quantity) {
        ShopCart cart = cartMapper.selectCartById(cartId);
        if (cart == null) {
            throw new ServiceException("購物車項目不存在");
        }

        // 檢查庫存
        ShopProductSku sku = skuMapper.selectSkuById(cart.getSkuId());
        if (sku == null || sku.getStockQuantity() < quantity) {
            throw new ServiceException("商品庫存不足");
        }

        return cartMapper.updateCartQuantity(cartId, quantity);
    }

    @Override
    public int updateCartSelected(Long cartId, boolean selected) {
        return cartMapper.updateCartSelected(cartId, selected);
    }

    @Override
    public int updateAllSelected(Long memberId, boolean selected) {
        return cartMapper.updateAllSelected(memberId, selected);
    }

    @Override
    public int deleteCartById(Long cartId) {
        return cartMapper.deleteCartById(cartId);
    }

    @Override
    public int deleteCartByIds(Long[] cartIds) {
        return cartMapper.deleteCartByIds(cartIds);
    }

    @Override
    public int clearCart(Long memberId) {
        log.info("清空購物車，會員ID: {}", memberId);
        return cartMapper.deleteCartByMemberId(memberId);
    }

    @Override
    public int deleteSelectedCarts(Long memberId) {
        return cartMapper.deleteSelectedCarts(memberId);
    }

    @Override
    public int countCartByMemberId(Long memberId) {
        return cartMapper.countCartByMemberId(memberId);
    }
}
