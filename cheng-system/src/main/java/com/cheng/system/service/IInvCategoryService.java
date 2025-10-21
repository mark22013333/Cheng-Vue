package com.cheng.system.service;

import com.cheng.system.domain.InvCategory;

import java.util.List;

/**
 * 物品分類 Service介面
 *
 * @author cheng
 */
public interface IInvCategoryService {
    /**
     * 查詢物品分類
     *
     * @param categoryId 物品分類主鍵
     * @return 物品分類
     */
    InvCategory selectInvCategoryByCategoryId(Long categoryId);

    /**
     * 查詢物品分類列表
     *
     * @param invCategory 物品分類
     * @return 物品分類集合
     */
    List<InvCategory> selectInvCategoryList(InvCategory invCategory);

    /**
     * 查詢已使用的物品分類列表（只返回在 inv_item 中有使用的分類）
     *
     * @return 物品分類集合
     */
    List<InvCategory> selectUsedCategoryList();

    /**
     * 新增物品分類
     *
     * @param invCategory 物品分類
     * @return 結果
     */
    int insertInvCategory(InvCategory invCategory);

    /**
     * 修改物品分類
     *
     * @param invCategory 物品分類
     * @return 結果
     */
    int updateInvCategory(InvCategory invCategory);

    /**
     * 批量刪除物品分類
     *
     * @param categoryIds 需要刪除的物品分類主鍵集合
     * @return 結果
     */
    int deleteInvCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 刪除物品分類訊息
     *
     * @param categoryId 物品分類主鍵
     * @return 結果
     */
    int deleteInvCategoryByCategoryId(Long categoryId);
}
