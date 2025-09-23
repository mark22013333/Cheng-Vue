package com.cheng.system.mapper;

import com.cheng.system.domain.SysUserPost;

import java.util.List;

/**
 * 使用者與職位關聯表 數據層
 *
 * @author cheng
 */
public interface SysUserPostMapper {
    /**
     * 通過使用者ID刪除使用者和職位關聯
     *
     * @param userId 使用者ID
     * @return 結果
     */
    int deleteUserPostByUserId(Long userId);

    /**
     * 通過職位ID查詢職位使用數量
     *
     * @param postId 職位ID
     * @return 結果
     */
    int countUserPostById(Long postId);

    /**
     * 批次刪除使用者和職位關聯
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    int deleteUserPost(Long[] ids);

    /**
     * 批次新增使用者職位訊息
     *
     * @param userPostList 使用者職位列表
     * @return 結果
     */
    int batchUserPost(List<SysUserPost> userPostList);
}
