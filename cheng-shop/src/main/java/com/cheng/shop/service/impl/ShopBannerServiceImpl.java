package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopBanner;
import com.cheng.shop.mapper.ShopBannerMapper;
import com.cheng.shop.service.IShopBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 輪播 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopBannerServiceImpl implements IShopBannerService {

    private final ShopBannerMapper bannerMapper;

    @Override
    public List<ShopBanner> selectBannerList(ShopBanner banner) {
        return bannerMapper.selectBannerList(banner);
    }

    @Override
    public ShopBanner selectBannerById(Long bannerId) {
        return bannerMapper.selectBannerById(bannerId);
    }

    @Override
    public List<ShopBanner> selectActiveBanners(String position) {
        return bannerMapper.selectActiveBanners(position);
    }

    @Override
    public int insertBanner(ShopBanner banner) {
        return bannerMapper.insertBanner(banner);
    }

    @Override
    public int updateBanner(ShopBanner banner) {
        return bannerMapper.updateBanner(banner);
    }

    @Override
    public int deleteBannerById(Long bannerId) {
        return bannerMapper.deleteBannerById(bannerId);
    }

    @Override
    public int deleteBannerByIds(Long[] bannerIds) {
        return bannerMapper.deleteBannerByIds(bannerIds);
    }
}
