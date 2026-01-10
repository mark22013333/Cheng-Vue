package com.cheng.line.service;

import com.cheng.line.domain.LineFlexTemplate;

import java.util.List;

/**
 * LINE Flex 自訂範本 Service 介面
 *
 * @author cheng
 */
public interface ILineFlexTemplateService {

    /**
     * 查詢 Flex 範本列表
     *
     * @param template 查詢條件
     * @return Flex 範本列表
     */
    List<LineFlexTemplate> selectLineFlexTemplateList(LineFlexTemplate template);

    /**
     * 查詢當前使用者可用的 Flex 範本（私人 + 公開）
     *
     * @return Flex 範本列表
     */
    List<LineFlexTemplate> selectAvailableFlexTemplates();

    /**
     * 查詢 Flex 範本詳情
     *
     * @param flexTemplateId 範本ID
     * @return Flex 範本
     */
    LineFlexTemplate selectLineFlexTemplateById(Long flexTemplateId);

    /**
     * 新增 Flex 範本
     *
     * @param template Flex 範本
     * @return 影響行數
     */
    int insertLineFlexTemplate(LineFlexTemplate template);

    /**
     * 修改 Flex 範本
     *
     * @param template Flex 範本
     * @return 影響行數
     */
    int updateLineFlexTemplate(LineFlexTemplate template);

    /**
     * 刪除 Flex 範本
     *
     * @param flexTemplateId 範本ID
     * @return 影響行數
     */
    int deleteLineFlexTemplateById(Long flexTemplateId);

    /**
     * 批次刪除 Flex 範本
     *
     * @param flexTemplateIds 範本ID 陣列
     * @return 影響行數
     */
    int deleteLineFlexTemplateByIds(Long[] flexTemplateIds);

    /**
     * 增加使用次數
     *
     * @param flexTemplateId 範本ID
     * @return 影響行數
     */
    int incrementUseCount(Long flexTemplateId);
}
