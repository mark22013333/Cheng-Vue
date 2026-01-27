package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.domain.ShopMemberAddress;
import com.cheng.shop.mapper.ShopMemberAddressMapper;
import com.cheng.shop.service.IShopMemberAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 會員收貨地址 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopMemberAddressServiceImpl implements IShopMemberAddressService {

    /**
     * 每個會員最多地址數量
     */
    private static final int MAX_ADDRESS_COUNT = 20;

    private final ShopMemberAddressMapper addressMapper;

    @Override
    public List<ShopMemberAddress> selectAddressListByMemberId(Long memberId) {
        return addressMapper.selectAddressListByMemberId(memberId);
    }

    @Override
    public ShopMemberAddress selectAddressById(Long addressId) {
        return addressMapper.selectAddressById(addressId);
    }

    @Override
    public ShopMemberAddress selectDefaultAddress(Long memberId) {
        return addressMapper.selectDefaultAddressByMemberId(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAddress(ShopMemberAddress address) {
        // 檢查地址數量上限
        int count = addressMapper.countByMemberId(address.getMemberId());
        if (count >= MAX_ADDRESS_COUNT) {
            throw new ServiceException("最多只能新增 " + MAX_ADDRESS_COUNT + " 個收貨地址");
        }

        // 如果是第一個地址或設為預設，先清除其他預設
        if (Boolean.TRUE.equals(address.getIsDefault()) || count == 0) {
            addressMapper.clearDefaultByMemberId(address.getMemberId());
            address.setIsDefault(true);
        }

        return addressMapper.insertAddress(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAddress(ShopMemberAddress address) {
        // 如果設為預設，先清除其他預設
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            ShopMemberAddress existing = addressMapper.selectAddressById(address.getAddressId());
            if (existing != null) {
                addressMapper.clearDefaultByMemberId(existing.getMemberId());
            }
        }

        return addressMapper.updateAddress(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAddressById(Long addressId) {
        ShopMemberAddress address = addressMapper.selectAddressById(addressId);
        if (address == null) {
            return 0;
        }

        int result = addressMapper.deleteAddressById(addressId);

        // 如果刪除的是預設地址，設定最新的一個為預設
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<ShopMemberAddress> remaining = addressMapper.selectAddressListByMemberId(address.getMemberId());
            if (!remaining.isEmpty()) {
                addressMapper.setDefaultAddress(remaining.get(0).getAddressId());
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultAddress(Long memberId, Long addressId) {
        addressMapper.clearDefaultByMemberId(memberId);
        return addressMapper.setDefaultAddress(addressId);
    }
}
