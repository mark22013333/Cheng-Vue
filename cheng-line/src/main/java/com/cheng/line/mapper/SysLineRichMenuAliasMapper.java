package com.cheng.line.mapper;

import com.cheng.line.domain.SysLineRichMenuAlias;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LINE Rich Menu Alias Mapper 介面
 * 
 * @author cheng
 */
@Mapper
public interface SysLineRichMenuAliasMapper {
    
    /**
     * 查詢 Rich Menu Alias
     * 
     * @param id Rich Menu Alias 主鍵
     * @return Rich Menu Alias
     */
    SysLineRichMenuAlias selectRichMenuAliasById(Long id);

    /**
     * 根據 Alias ID 查詢
     * 
     * @param aliasId Alias ID
     * @return Rich Menu Alias
     */
    SysLineRichMenuAlias selectRichMenuAliasByAliasId(String aliasId);

    /**
     * 查詢 Rich Menu Alias 列表
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return Rich Menu Alias 集合
     */
    List<SysLineRichMenuAlias> selectRichMenuAliasList(SysLineRichMenuAlias richMenuAlias);

    /**
     * 根據 Rich Menu ID 查詢所有 Alias
     * 
     * @param richMenuId Rich Menu ID
     * @return Rich Menu Alias 集合
     */
    List<SysLineRichMenuAlias> selectRichMenuAliasByRichMenuId(Long richMenuId);

    /**
     * 新增 Rich Menu Alias
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return 結果
     */
    int insertRichMenuAlias(SysLineRichMenuAlias richMenuAlias);

    /**
     * 修改 Rich Menu Alias
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return 結果
     */
    int updateRichMenuAlias(SysLineRichMenuAlias richMenuAlias);

    /**
     * 刪除 Rich Menu Alias
     * 
     * @param id Rich Menu Alias 主鍵
     * @return 結果
     */
    int deleteRichMenuAliasById(Long id);

    /**
     * 根據 Alias ID 刪除
     * 
     * @param aliasId Alias ID
     * @return 結果
     */
    int deleteRichMenuAliasByAliasId(String aliasId);

    /**
     * 批次刪除 Rich Menu Alias
     * 
     * @param ids 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteRichMenuAliasByIds(@Param("ids") Long[] ids);

    /**
     * 檢查 Alias ID 是否唯一
     * 
     * @param aliasId Alias ID
     * @return 結果
     */
    int checkAliasIdUnique(String aliasId);
}
