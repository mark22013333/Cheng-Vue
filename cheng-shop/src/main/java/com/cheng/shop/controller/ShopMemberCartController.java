package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopCart;
import com.cheng.shop.domain.dto.AddCartRequest;
import com.cheng.shop.domain.dto.UpdateCartRequest;
import com.cheng.shop.domain.vo.CartVO;
import com.cheng.shop.service.IShopCartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 會員購物車 Controller（前台，只需登入不需特定權限）
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/member/cart")
@RequiredArgsConstructor
public class ShopMemberCartController extends BaseController {

    private final IShopCartService cartService;

    /**
     * 取得購物車內容
     */
    @GetMapping
    public AjaxResult getCart() {
        Long memberId = SecurityUtils.getUserId();
        List<ShopCart> cartList = cartService.selectCartListByMemberId(memberId);

        // 計算統計資訊
        int totalQuantity = 0;
        int selectedQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal selectedAmount = BigDecimal.ZERO;

        for (ShopCart item : cartList) {
            totalQuantity += item.getQuantity();
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            if (Boolean.TRUE.equals(item.getIsSelected())) {
                selectedQuantity += item.getQuantity();
                selectedAmount = selectedAmount.add(itemTotal);
            }
        }

        CartVO cartVO = CartVO.builder()
                .items(cartList)
                .totalQuantity(totalQuantity)
                .selectedQuantity(selectedQuantity)
                .totalAmount(totalAmount)
                .selectedAmount(selectedAmount)
                .build();

        return success(cartVO);
    }

    /**
     * 取得購物車商品數量
     */
    @GetMapping("/count")
    public AjaxResult getCartCount() {
        Long memberId = SecurityUtils.getUserId();
        int count = cartService.countCartByMemberId(memberId);
        return success(count);
    }

    /**
     * 加入購物車
     */
    @Log(title = "會員購物車", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addToCart(@Valid @RequestBody AddCartRequest request) {
        Long memberId = SecurityUtils.getUserId();
        int result = cartService.addToCart(memberId, request.getSkuId(), request.getQuantity());
        return toAjax(result);
    }

    /**
     * 更新購物車數量
     */
    @Log(title = "會員購物車", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public AjaxResult updateQuantity(@Valid @RequestBody UpdateCartRequest request) {
        int result = cartService.updateCartQuantity(request.getCartId(), request.getQuantity());
        return toAjax(result);
    }

    /**
     * 更新選中狀態
     */
    @PutMapping("/select/{cartId}")
    public AjaxResult updateSelected(@PathVariable Long cartId, @RequestParam boolean selected) {
        int result = cartService.updateCartSelected(cartId, selected);
        return toAjax(result);
    }

    /**
     * 全選/取消全選
     */
    @PutMapping("/selectAll")
    public AjaxResult selectAll(@RequestParam boolean selected) {
        Long memberId = SecurityUtils.getUserId();
        int result = cartService.updateAllSelected(memberId, selected);
        return toAjax(result);
    }

    /**
     * 移除購物車項目
     */
    @Log(title = "會員購物車", businessType = BusinessType.DELETE)
    @DeleteMapping("/remove/{cartId}")
    public AjaxResult removeItem(@PathVariable Long cartId) {
        int result = cartService.deleteCartById(cartId);
        return toAjax(result);
    }

    /**
     * 批量刪除購物車項目
     */
    @Log(title = "會員購物車", businessType = BusinessType.DELETE)
    @DeleteMapping("/remove")
    public AjaxResult removeItems(@RequestBody Long[] cartIds) {
        int result = cartService.deleteCartByIds(cartIds);
        return toAjax(result);
    }

    /**
     * 清空購物車
     */
    @Log(title = "會員購物車", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear")
    public AjaxResult clearCart() {
        Long memberId = SecurityUtils.getUserId();
        int result = cartService.clearCart(memberId);
        return toAjax(result);
    }

    /**
     * 刪除已選中項目
     */
    @Log(title = "會員購物車", businessType = BusinessType.DELETE)
    @DeleteMapping("/removeSelected")
    public AjaxResult removeSelected() {
        Long memberId = SecurityUtils.getUserId();
        int result = cartService.deleteSelectedCarts(memberId);
        return toAjax(result);
    }

    /**
     * 取得已選中的購物車項目（結帳用）
     */
    @GetMapping("/selected")
    public AjaxResult getSelectedItems() {
        Long memberId = SecurityUtils.getUserId();
        List<ShopCart> items = cartService.selectSelectedCartsByMemberId(memberId);
        return success(items);
    }

    /**
     * 合併訪客購物車（登入後呼叫）
     */
    @Log(title = "會員購物車", businessType = BusinessType.INSERT)
    @PostMapping("/merge")
    public AjaxResult mergeGuestCart(@RequestBody List<AddCartRequest> items) {
        Long memberId = SecurityUtils.getUserId();
        int successCount = 0;
        for (AddCartRequest item : items) {
            try {
                cartService.addToCart(memberId, item.getSkuId(), item.getQuantity());
                successCount++;
            } catch (Exception e) {
                log.warn("合併購物車項目失敗: skuId={}, error={}", item.getSkuId(), e.getMessage());
            }
        }
        return success(successCount);
    }
}
