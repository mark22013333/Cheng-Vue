package com.cheng.line.service;

import com.cheng.line.domain.LineTemplateImagemapRef;

import java.util.List;
import java.util.Map;

/**
 * 訊息範本與圖文範本關聯 Service 介面
 *
 * @author cheng
 */
public interface ILineTemplateImagemapRefService {

    /**
     * 根據圖文範本ID查詢引用列表（哪些訊息範本使用了這個圖文範本）
     *
     * @param imagemapId 圖文範本ID
     * @return 引用列表
     */
    List<LineTemplateImagemapRef> selectRefsByImagemapId(Long imagemapId);

    /**
     * 根據訊息範本ID查詢引用列表
     *
     * @param templateId 訊息範本ID
     * @return 引用列表
     */
    List<LineTemplateImagemapRef> selectRefsByTemplateId(Long templateId);

    /**
     * 同步更新引用的訊息範本（當圖文範本異動時）
     *
     * @param imagemapId  圖文範本ID
     * @param templateIds 要同步的訊息範本ID列表（null 表示全部）
     * @return 同步的範本數量
     */
    int syncReferences(Long imagemapId, List<Long> templateIds);

    /**
     * 維護關聯（儲存訊息範本時呼叫）
     *
     * @param templateId  訊息範本ID
     * @param imagemapIds 引用的圖文範本ID列表
     */
    void maintainRefs(Long templateId, List<Long> imagemapIds);

    /**
     * 維護關聯（含訊息索引位置）
     *
     * @param templateId   訊息範本ID
     * @param imagemapRefs 引用資訊列表（包含 imagemapId 和 messageIndex）
     */
    void maintainRefsWithIndex(Long templateId, List<Map<String, Object>> imagemapRefs);

    /**
     * 刪除訊息範本的所有關聯
     *
     * @param templateId 訊息範本ID
     * @return 刪除的數量
     */
    int deleteRefsByTemplateId(Long templateId);
}
