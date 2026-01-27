package com.cheng.shop.service;

import com.cheng.shop.domain.ShopMemberAddress;

import java.util.List;

/**
 * 會員收貨地址 Service 介面
 *
 * @author cheng
 */
public interface IShopMemberAddressService {

    /**
     * 根據會員 ID 查詢地址列表
     *
     * @param memberId 會員 ID
     * @return 地址列表
     */
    List<ShopMemberAddress> selectAddressListByMemberId(Long memberId);

    /**
     * 根據地址 ID 查詢
     *
     * @param addressId 地址 ID
     * @return 地址資訊
     */
    ShopMemberAddress selectAddressById(Long addressId);

    /**
     * 查詢會員預設地址
     *
     * @param memberId 會員 ID
     * @return 預設地址
     */
    ShopMemberAddress selectDefaultAddress(Long memberId);

    /**
     * 新增地址
     *
     * @param address 地址資訊
     * @return 影響行數
     */
    int insertAddress(ShopMemberAddress address);

    /**
     * 更新地址
     *
     * @param address 地址資訊
     * @return 影響行數
     */
    int updateAddress(ShopMemberAddress address);

    /**
     * 刪除地址
     *
     * @param addressId 地址 ID
     * @return 影響行數
     */
    int deleteAddressById(Long addressId);

    /**
     * 設定預設地址
     *
     * @param memberId  會員 ID
     * @param addressId 地址 ID
     * @return 影響行數
     */
    int setDefaultAddress(Long memberId, Long addressId);
}
