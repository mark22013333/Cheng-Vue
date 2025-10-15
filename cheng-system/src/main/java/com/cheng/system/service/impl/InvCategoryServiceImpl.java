package com.cheng.system.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.system.domain.InvCategory;
import com.cheng.system.domain.InvItem;
import com.cheng.system.mapper.InvCategoryMapper;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IInvCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 物品分類 Service業務層處理
 *
 * @author cheng
 */
@Service
public class InvCategoryServiceImpl implements IInvCategoryService {
    @Autowired
    private InvCategoryMapper invCategoryMapper;
    
    @Autowired
    private InvItemMapper invItemMapper;

    /**
     * 查詢物品分類
     *
     * @param categoryId 物品分類主鍵
     * @return 物品分類
     */
    @Override
    public InvCategory selectInvCategoryByCategoryId(Long categoryId) {
        return invCategoryMapper.selectInvCategoryByCategoryId(categoryId);
    }

    /**
     * 查詢物品分類列表
     *
     * @param invCategory 物品分類
     * @return 物品分類
     */
    @Override
    public List<InvCategory> selectInvCategoryList(InvCategory invCategory) {
        return invCategoryMapper.selectInvCategoryList(invCategory);
    }

    /**
     * 新增物品分類
     *
     * @param invCategory 物品分類
     * @return 結果
     */
    @Override
    @Transactional
    public int insertInvCategory(InvCategory invCategory) {
        // 設定建立者和建立時間
        invCategory.setCreateBy(SecurityUtils.getUsername());
        invCategory.setCreateTime(new Date());
        invCategory.setDelFlag("0");
        
        // 處理預設分類邏輯
        handleDefaultCategory(invCategory);
        
        return invCategoryMapper.insertInvCategory(invCategory);
    }

    /**
     * 修改物品分類
     *
     * @param invCategory 物品分類
     * @return 結果
     */
    @Override
    @Transactional
    public int updateInvCategory(InvCategory invCategory) {
        // 設定更新者和更新時間
        invCategory.setUpdateBy(SecurityUtils.getUsername());
        invCategory.setUpdateTime(new Date());
        
        // 處理預設分類邏輯
        handleDefaultCategory(invCategory);
        
        return invCategoryMapper.updateInvCategory(invCategory);
    }
    
    /**
     * 處理預設分類邏輯
     * 如果設定為預設分類，則清除其他分類的預設標記
     */
    private void handleDefaultCategory(InvCategory invCategory) {
        if (invCategory.getRemark() != null && invCategory.getRemark().contains("預設分類")) {
            // 查詢所有分類
            InvCategory query = new InvCategory();
            List<InvCategory> allCategories = invCategoryMapper.selectInvCategoryList(query);
            
            // 清除其他分類的預設標記
            for (InvCategory category : allCategories) {
                // 跳過當前分類
                if (invCategory.getCategoryId() != null && 
                    category.getCategoryId().equals(invCategory.getCategoryId())) {
                    continue;
                }
                
                // 如果其他分類有預設標記，則移除
                if (category.getRemark() != null && category.getRemark().contains("預設分類")) {
                    String newRemark = category.getRemark().replaceAll("；?預設分類", "").trim();
                    category.setRemark(newRemark.isEmpty() ? null : newRemark);
                    category.setUpdateBy(SecurityUtils.getUsername());
                    category.setUpdateTime(new Date());
                    invCategoryMapper.updateInvCategory(category);
                }
            }
        }
    }

    /**
     * 批量刪除物品分類
     *
     * @param categoryIds 需要刪除的物品分類主鍵
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteInvCategoryByCategoryIds(Long[] categoryIds) {
        // 檢查每個分類是否被物品使用
        for (Long categoryId : categoryIds) {
            checkCategoryInUse(categoryId);
        }
        return invCategoryMapper.deleteInvCategoryByCategoryIds(categoryIds);
    }

    /**
     * 刪除物品分類訊息
     *
     * @param categoryId 物品分類主鍵
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteInvCategoryByCategoryId(Long categoryId) {
        // 檢查分類是否被物品使用
        checkCategoryInUse(categoryId);
        return invCategoryMapper.deleteInvCategoryByCategoryId(categoryId);
    }
    
    /**
     * 檢查分類是否被物品使用
     *
     * @param categoryId 分類ID
     */
    private void checkCategoryInUse(Long categoryId) {
        InvCategory category = invCategoryMapper.selectInvCategoryByCategoryId(categoryId);
        if (category == null) {
            throw new ServiceException("分類不存在");
        }
        
        // 查詢使用此分類的物品
        InvItem query = new InvItem();
        query.setCategoryId(categoryId);
        List<InvItem> items = invItemMapper.selectInvItemList(query);
        
        if (items != null && !items.isEmpty()) {
            throw new ServiceException(String.format("分類「%s」正被 %d 個物品使用，無法刪除", 
                category.getCategoryName(), items.size()));
        }
    }
}
