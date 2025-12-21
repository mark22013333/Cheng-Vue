package com.cheng.line.mapper;

import com.cheng.line.domain.LineMessageTemplate;

import java.util.List;

/**
 * LINE 訊息範本 Mapper 介面
 *
 * @author cheng
 */
public interface LineMessageTemplateMapper {

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
     * @param templateCode 範本代碼
     * @return 範本
     */
    LineMessageTemplate checkTemplateCodeUnique(String templateCode);

    /**
     * 增加使用次數
     *
     * @param templateId 範本ID
     * @return 結果
     */
    int incrementUseCount(Long templateId);
}
