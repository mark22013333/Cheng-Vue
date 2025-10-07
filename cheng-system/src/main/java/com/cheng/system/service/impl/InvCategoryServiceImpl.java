package com.cheng.system.service.impl;

import com.cheng.system.domain.InvCategory;
import com.cheng.system.mapper.InvCategoryMapper;
import com.cheng.system.service.IInvCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public int insertInvCategory(InvCategory invCategory) {
        return invCategoryMapper.insertInvCategory(invCategory);
    }

    /**
     * 修改物品分類
     *
     * @param invCategory 物品分類
     * @return 結果
     */
    @Override
    public int updateInvCategory(InvCategory invCategory) {
        return invCategoryMapper.updateInvCategory(invCategory);
    }

    /**
     * 批量刪除物品分類
     *
     * @param categoryIds 需要刪除的物品分類主鍵
     * @return 結果
     */
    @Override
    public int deleteInvCategoryByCategoryIds(Long[] categoryIds) {
        return invCategoryMapper.deleteInvCategoryByCategoryIds(categoryIds);
    }

    /**
     * 刪除物品分類訊息
     *
     * @param categoryId 物品分類主鍵
     * @return 結果
     */
    @Override
    public int deleteInvCategoryByCategoryId(Long categoryId) {
        return invCategoryMapper.deleteInvCategoryByCategoryId(categoryId);
    }
}
