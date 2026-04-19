package com.cheng.line.service;

import com.cheng.line.dto.TagPreviewDTO;

import java.util.List;
import java.util.Set;

/**
 * LINE 標籤目標解析 Service 介面
 * 根據標籤和標籤群組解析目標使用者
 *
 * @author cheng
 */
public interface ILineTagResolveService {

    /**
     * 解析標籤和標籤群組的目標使用者
     * 將個別標籤和群組的結果取聯集（去重）
     *
     * @param tagIds      個別標籤 ID 列表
     * @param tagGroupIds 標籤群組 ID 列表
     * @return 不重複的 LINE User ID 集合
     */
    Set<String> resolveTargets(List<Long> tagIds, List<Long> tagGroupIds);

    /**
     * 預覽標籤推播的目標人數
     * 僅回傳預計人數和各標籤明細，不觸發發送
     *
     * @param tagIds      個別標籤 ID 列表
     * @param tagGroupIds 標籤群組 ID 列表
     * @return 預覽結果
     */
    TagPreviewDTO previewCount(List<Long> tagIds, List<Long> tagGroupIds);
}
