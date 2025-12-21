package com.cheng.line.service;

import com.cheng.line.domain.LineMessageTemplate;

import java.util.List;

/**
 * LINE 訊息範本 服務層
 *
 * @author cheng
 */
public interface ILineMessageTemplateService {

    /**
     * 查詢範本
     *
     * @param templateId 範本ID
     * @return 範本
     */
    LineMessageTemplate selectLineMessageTemplateById(Long templateId);

    /**
     * 根據範本代碼查詢
     *
     * @param templateCode 範本代碼
     * @return 範本
     */
    LineMessageTemplate selectLineMessageTemplateByCode(String templateCode);

    /**
     * 查詢範本列表
     *
     * @param lineMessageTemplate 範本查詢條件
     * @return 範本集合
     */
    List<LineMessageTemplate> selectLineMessageTemplateList(LineMessageTemplate lineMessageTemplate);

    /**
     * 根據訊息類型查詢範本列表
     *
     * @param msgType 訊息類型
     * @return 範本集合
     */
    List<LineMessageTemplate> selectLineMessageTemplateByType(String msgType);

    /**
     * 新增範本
     *
     * @param lineMessageTemplate 範本
     * @return 結果
     */
    int insertLineMessageTemplate(LineMessageTemplate lineMessageTemplate);

    /**
     * 修改範本
     *
     * @param lineMessageTemplate 範本
     * @return 結果
     */
    int updateLineMessageTemplate(LineMessageTemplate lineMessageTemplate);

    /**
     * 刪除範本
     *
     * @param templateId 範本ID
     * @return 結果
     */
    int deleteLineMessageTemplateById(Long templateId);

    /**
     * 批次刪除範本
     *
     * @param templateIds 範本ID陣列
     * @return 結果
     */
    int deleteLineMessageTemplateByIds(Long[] templateIds);

    /**
     * 檢查範本代碼是否唯一
     *
     * @param lineMessageTemplate 範本
     * @return 結果
     */
    boolean checkTemplateCodeUnique(LineMessageTemplate lineMessageTemplate);

    /**
     * 增加使用次數
     *
     * @param templateId 範本ID
     * @return 結果
     */
    int incrementUseCount(Long templateId);

    /**
     * 解析範本內容並替換變數
     *
     * @param templateId  範本ID
     * @param lineUserId  LINE 使用者ID（用於取得使用者資訊）
     * @return 替換變數後的內容
     */
    String parseTemplateContent(Long templateId, String lineUserId);

    /**
     * 解析範本內容並替換變數（使用自訂變數）
     *
     * @param content   範本內容
     * @param variables 變數 Map
     * @return 替換變數後的內容
     */
    String parseTemplateContent(String content, java.util.Map<String, String> variables);

    /**
     * 取得預設 Flex 範本列表
     *
     * @return 範本列表（名稱和描述）
     */
    List<java.util.Map<String, String>> getFlexPresetTemplates();

    /**
     * 取得指定 Flex 範本內容
     *
     * @param templateName 範本名稱
     * @return 範本 JSON 內容
     */
    String getFlexPresetContent(String templateName);
}
