package com.cheng.shop.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.enums.CommonStatus;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import com.cheng.shop.service.IShopProductSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品SKU Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopProductSkuServiceImpl implements IShopProductSkuService {

    private final ShopProductSkuMapper skuMapper;

    @Override
    public List<ShopProductSku> selectSkuListByProductId(Long productId) {
        return skuMapper.selectSkuListByProductId(productId);
    }

    @Override
    public ShopProductSku selectSkuById(Long skuId) {
        return skuMapper.selectSkuById(skuId);
    }

    @Override
    public int insertSku(ShopProductSku sku) {
        if (sku.getStatus() == null) {
            sku.setStatus(CommonStatus.ENABLED.getCode());
        }
        if (sku.getSortOrder() == null) {
            sku.setSortOrder(0);
        }
        if (sku.getSalesCount() == null) {
            sku.setSalesCount(0);
        }
        if (sku.getStockQuantity() == null) {
            sku.setStockQuantity(0);
        }
        sku.setCreateTime(DateUtils.getNowDate());
        return skuMapper.insertSku(sku);
    }

    @Override
    public int updateSku(ShopProductSku sku) {
        sku.setUpdateTime(DateUtils.getNowDate());
        return skuMapper.updateSku(sku);
    }

    @Override
    public int deleteSkuById(Long skuId) {
        return skuMapper.deleteSkuById(skuId);
    }

    @Override
    public int deleteSkuByProductId(Long productId) {
        return skuMapper.deleteSkuByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveSku(Long productId, List<ShopProductSku> skuList) {
        skuMapper.deleteSkuByProductId(productId);
        
        if (skuList == null || skuList.isEmpty()) {
            return 0;
        }

        for (ShopProductSku sku : skuList) {
            sku.setProductId(productId);
            if (sku.getStatus() == null) {
                sku.setStatus(CommonStatus.ENABLED.getCode());
            }
            if (sku.getSortOrder() == null) {
                sku.setSortOrder(0);
            }
            if (sku.getSalesCount() == null) {
                sku.setSalesCount(0);
            }
            if (sku.getStockQuantity() == null) {
                sku.setStockQuantity(0);
            }
        }

        return skuMapper.batchInsertSku(skuList);
    }

    @Override
    public boolean checkSkuCodeUnique(ShopProductSku sku) {
        Long skuId = sku.getSkuId() == null ? -1L : sku.getSkuId();
        int count = skuMapper.checkSkuCodeUnique(sku.getSkuCode(), skuId);
        return count == 0;
    }
}
