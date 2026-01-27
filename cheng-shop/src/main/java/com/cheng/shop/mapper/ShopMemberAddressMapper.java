package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopMemberAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 會員收貨地址 Mapper
 *
 * @author cheng
 */
@Mapper
public interface ShopMemberAddressMapper {

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
    ShopMemberAddress selectDefaultAddressByMemberId(Long memberId);

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
     * 取消會員所有地址的預設狀態
     *
     * @param memberId 會員 ID
     * @return 影響行數
     */
    int clearDefaultByMemberId(Long memberId);

    /**
     * 設定預設地址
     *
     * @param addressId 地址 ID
     * @return 影響行數
     */
    int setDefaultAddress(Long addressId);

    /**
     * 統計會員地址數量
     *
     * @param memberId 會員 ID
     * @return 地址數量
     */
    int countByMemberId(Long memberId);
}
