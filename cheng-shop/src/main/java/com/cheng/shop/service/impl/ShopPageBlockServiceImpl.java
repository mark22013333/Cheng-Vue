package com.cheng.shop.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.shop.domain.ShopPageBlock;
import com.cheng.shop.mapper.ShopPageBlockMapper;
import com.cheng.shop.service.IShopPageBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 頁面區塊 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopPageBlockServiceImpl implements IShopPageBlockService {

    private final ShopPageBlockMapper blockMapper;

    @Override
    public List<ShopPageBlock> selectBlockList(ShopPageBlock block) {
        return blockMapper.selectBlockList(block);
    }

    @Override
    public ShopPageBlock selectBlockById(Long blockId) {
        return blockMapper.selectBlockById(blockId);
    }

    @Override
    public ShopPageBlock selectBlockByPageAndKey(String pageKey, String blockKey) {
        return blockMapper.selectBlockByPageAndKey(pageKey, blockKey);
    }

    @Override
    public int insertBlock(ShopPageBlock block) {
        block.setCreateTime(DateUtils.getNowDate());
        return blockMapper.insertBlock(block);
    }

    @Override
    public int updateBlock(ShopPageBlock block) {
        block.setUpdateTime(DateUtils.getNowDate());
        return blockMapper.updateBlock(block);
    }

    @Override
    public int deleteBlockById(Long blockId) {
        return blockMapper.deleteBlockById(blockId);
    }

    @Override
    public boolean checkBlockKeyUnique(ShopPageBlock block) {
        Long blockId = block.getBlockId() == null ? -1L : block.getBlockId();
        int count = blockMapper.checkBlockKeyUnique(block.getPageKey(), block.getBlockKey(), blockId);
        return count == 0;
    }
}
