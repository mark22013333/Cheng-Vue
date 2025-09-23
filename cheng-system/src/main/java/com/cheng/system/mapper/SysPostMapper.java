package com.cheng.system.mapper;

import com.cheng.system.domain.SysPost;

import java.util.List;

/**
 * 職位訊息 數據層
 *
 * @author cheng
 */
public interface SysPostMapper {
    /**
     * 查詢職位數據集合
     *
     * @param post 職位訊息
     * @return 職位數據集合
     */
    List<SysPost> selectPostList(SysPost post);

    /**
     * 查詢所有職位
     *
     * @return 職位列表
     */
    List<SysPost> selectPostAll();

    /**
     * 通過職位ID查詢職位訊息
     *
     * @param postId 職位ID
     * @return 角色物件訊息
     */
    SysPost selectPostById(Long postId);

    /**
     * 根據使用者ID取得職位選擇框列表
     *
     * @param userId 使用者ID
     * @return 選中職位ID列表
     */
    List<Long> selectPostListByUserId(Long userId);

    /**
     * 查詢使用者所屬職位組
     *
     * @param userName 使用者名
     * @return 結果
     */
    List<SysPost> selectPostsByUserName(String userName);

    /**
     * 刪除職位訊息
     *
     * @param postId 職位ID
     * @return 結果
     */
    int deletePostById(Long postId);

    /**
     * 批次刪除職位訊息
     *
     * @param postIds 需要刪除的職位ID
     * @return 結果
     */
    int deletePostByIds(Long[] postIds);

    /**
     * 修改職位訊息
     *
     * @param post 職位訊息
     * @return 結果
     */
    int updatePost(SysPost post);

    /**
     * 新增職位訊息
     *
     * @param post 職位訊息
     * @return 結果
     */
    int insertPost(SysPost post);

    /**
     * 校驗職位名稱
     *
     * @param postName 職位名稱
     * @return 結果
     */
    SysPost checkPostNameUnique(String postName);

    /**
     * 校驗職位編碼
     *
     * @param postCode 職位編碼
     * @return 結果
     */
    SysPost checkPostCodeUnique(String postCode);
}
