package com.cheng.line.mapper;

import com.cheng.line.domain.LineTemplateImagemapRef;

import java.util.List;

/**
 * 訊息範本與圖文範本關聯 Mapper 介面
 *
 * @author cheng
 */
public interface LineTemplateImagemapRefMapper {

    /**
     * 根據圖文範本ID查詢引用列表
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
     * 新增關聯
     *
     * @param ref 關聯物件
     * @return 結果
     */
    int insertRef(LineTemplateImagemapRef ref);

    /**
     * 刪除關聯
     *
     * @param refId 關聯ID
     * @return 結果
     */
    int deleteRefById(Long refId);

    /**
     * 根據訊息範本ID刪除所有關聯
     *
     * @param templateId 訊息範本ID
     * @return 結果
     */
    int deleteRefsByTemplateId(Long templateId);

    /**
     * 根據圖文範本ID刪除所有關聯
     *
     * @param imagemapId 圖文範本ID
     * @return 結果
     */
    int deleteRefsByImagemapId(Long imagemapId);

    /**
     * 批次新增關聯
     *
     * @param refs 關聯列表
     * @return 結果
     */
    int batchInsertRefs(List<LineTemplateImagemapRef> refs);
}
