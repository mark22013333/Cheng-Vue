package com.cheng.web.controller.shop;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopMemberAddress;
import com.cheng.shop.domain.dto.AddressRequest;
import com.cheng.shop.service.IShopMemberAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員收貨地址 Controller（會員前台，只需登入不需特定權限）
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/address")
@RequiredArgsConstructor
public class ShopMemberAddressController extends BaseController {

    private final IShopMemberAddressService addressService;

    /**
     * 取得當前會員的地址列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        Long memberId = SecurityUtils.getUserId();
        List<ShopMemberAddress> list = addressService.selectAddressListByMemberId(memberId);
        return success(list);
    }

    /**
     * 取得地址詳情
     */
    @GetMapping("/{addressId}")
    public AjaxResult getInfo(@PathVariable Long addressId) {
        ShopMemberAddress address = addressService.selectAddressById(addressId);
        // 檢查是否屬於當前會員
        if (address != null && !address.getMemberId().equals(SecurityUtils.getUserId())) {
            return error("無權限查看此地址");
        }
        return success(address);
    }

    /**
     * 取得預設地址
     */
    @GetMapping("/default")
    public AjaxResult getDefault() {
        Long memberId = SecurityUtils.getUserId();
        ShopMemberAddress address = addressService.selectDefaultAddress(memberId);
        return success(address);
    }

    /**
     * 新增地址
     */
    @Log(title = "會員地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AddressRequest request) {
        ShopMemberAddress address = new ShopMemberAddress();
        BeanUtils.copyProperties(request, address);
        address.setMemberId(SecurityUtils.getUserId());
        return toAjax(addressService.insertAddress(address));
    }

    /**
     * 更新地址
     */
    @Log(title = "會員地址", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AddressRequest request) {
        if (request.getAddressId() == null) {
            return error("地址 ID 不能為空");
        }

        // 檢查是否屬於當前會員
        ShopMemberAddress existing = addressService.selectAddressById(request.getAddressId());
        if (existing == null || !existing.getMemberId().equals(SecurityUtils.getUserId())) {
            return error("無權限修改此地址");
        }

        ShopMemberAddress address = new ShopMemberAddress();
        BeanUtils.copyProperties(request, address);
        return toAjax(addressService.updateAddress(address));
    }

    /**
     * 刪除地址
     */
    @Log(title = "會員地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{addressId}")
    public AjaxResult remove(@PathVariable Long addressId) {
        // 檢查是否屬於當前會員
        ShopMemberAddress existing = addressService.selectAddressById(addressId);
        if (existing == null || !existing.getMemberId().equals(SecurityUtils.getUserId())) {
            return error("無權限刪除此地址");
        }

        return toAjax(addressService.deleteAddressById(addressId));
    }

    /**
     * 設定預設地址
     */
    @Log(title = "會員地址", businessType = BusinessType.UPDATE)
    @PutMapping("/default/{addressId}")
    public AjaxResult setDefault(@PathVariable Long addressId) {
        Long memberId = SecurityUtils.getUserId();

        // 檢查是否屬於當前會員
        ShopMemberAddress existing = addressService.selectAddressById(addressId);
        if (existing == null || !existing.getMemberId().equals(memberId)) {
            return error("無權限設定此地址");
        }

        return toAjax(addressService.setDefaultAddress(memberId, addressId));
    }
}
