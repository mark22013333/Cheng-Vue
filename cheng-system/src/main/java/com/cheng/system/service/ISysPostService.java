package com.cheng.system.service;

import com.cheng.system.domain.SysPost;

import java.util.List;

/**
 * 職位訊息 服務層
 *
 * @author cheng
 */
public interface ISysPostService
{
    /**
     * 查詢職位訊息集合
     *
     * @param post 職位訊息
     * @return 職位列表
     */
    public List<SysPost> selectPostList(SysPost post);

    /**
     * 查詢所有職位
     *
     * @return 職位列表
     */
    public List<SysPost> selectPostAll();

    /**
     * 通過職位ID查詢職位訊息
     *
     * @param postId 職位ID
     * @return 角色物件訊息
     */
    public SysPost selectPostById(Long postId);

    /**
     * 根據使用者ID取得職位選擇框列表
     *
     * @param userId 使用者ID
     * @return 選中職位ID列表
     */
    public List<Long> selectPostListByUserId(Long userId);

    /**
     * 校驗職位名稱
     *
     * @param post 職位訊息
     * @return 結果
     */
    public boolean checkPostNameUnique(SysPost post);

    /**
     * 校驗職位編碼
     *
     * @param post 職位訊息
     * @return 結果
     */
    public boolean checkPostCodeUnique(SysPost post);

    /**
     * 通過職位ID查詢職位使用數量
     *
     * @param postId 職位ID
     * @return 結果
     */
    public int countUserPostById(Long postId);

    /**
     * 刪除職位訊息
     *
     * @param postId 職位ID
     * @return 結果
     */
    public int deletePostById(Long postId);

    /**
     * 批量刪除職位訊息
     *
     * @param postIds 需要刪除的職位ID
     * @return 結果
     */
    public int deletePostByIds(Long[] postIds);

    /**
     * 新增儲存職位訊息
     *
     * @param post 職位訊息
     * @return 結果
     */
    public int insertPost(SysPost post);

    /**
     * 修改儲存職位訊息
     *
     * @param post 職位訊息
     * @return 結果
     */
    public int updatePost(SysPost post);
}
