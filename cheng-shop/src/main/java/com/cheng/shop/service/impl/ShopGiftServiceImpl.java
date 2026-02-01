package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopGift;
import com.cheng.shop.mapper.ShopGiftMapper;
import com.cheng.shop.service.IShopGiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 滿額禮物 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopGiftServiceImpl implements IShopGiftService {

    private final ShopGiftMapper giftMapper;

    @Override
    public List<ShopGift> selectGiftList(ShopGift gift) {
        return giftMapper.selectGiftList(gift);
    }

    @Override
    public ShopGift selectGiftById(Long giftId) {
        return giftMapper.selectGiftById(giftId);
    }

    @Override
    public List<ShopGift> selectAvailableGifts(BigDecimal amount) {
        return giftMapper.selectAvailableGifts(amount);
    }

    @Override
    public int insertGift(ShopGift gift) {
        return giftMapper.insertGift(gift);
    }

    @Override
    public int updateGift(ShopGift gift) {
        return giftMapper.updateGift(gift);
    }

    @Override
    public int deleteGiftById(Long giftId) {
        return giftMapper.deleteGiftById(giftId);
    }

    @Override
    public int deleteGiftByIds(Long[] giftIds) {
        return giftMapper.deleteGiftByIds(giftIds);
    }

    @Override
    public int decreaseStock(Long giftId, int quantity) {
        return giftMapper.decreaseStock(giftId, quantity);
    }
}
