package com.cheng.system.mapper;

import com.cheng.system.domain.InvCategory;

import java.util.List;

/**
 * 物品分類 數據層
 *
 * @author cheng
 */
public interface InvCategoryMapper {
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

//    /**
//     * 查詢子分類數量
//     *
//     * @param parentId 父分類ID
//     * @return 子分類數量
//     */
//    int selectChildrenCountByParentId(Long parentId);
//
//    /**
//     * 檢查分類編碼是否唯一
//     *
//     * @param invCategory 物品分類資訊
//     * @return 結果
//     */
//    InvCategory checkCategoryCodeUnique(InvCategory invCategory);

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
     * 刪除物品分類
     *
     * @param categoryId 物品分類主鍵
     * @return 結果
     */
    int deleteInvCategoryByCategoryId(Long categoryId);

    /**
     * 批量刪除物品分類
     *
     * @param categoryIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvCategoryByCategoryIds(Long[] categoryIds);
}
