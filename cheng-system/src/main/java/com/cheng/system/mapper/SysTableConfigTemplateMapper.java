package com.cheng.system.mapper;

import com.cheng.system.domain.SysTableConfigTemplate;
import org.apache.ibatis.annotations.Param;

/**
 * 表格欄位配置模版Mapper介面
 *
 * @author cheng
 */
public interface SysTableConfigTemplateMapper {

    /**
     * 根據頁面標識查詢模版配置
     *
     * @param pageKey 頁面標識
     * @return 模版配置
     */
    SysTableConfigTemplate selectByPageKey(@Param("pageKey") String pageKey);

    /**
     * 新增或更新模版配置
     *
     * @param template 模版配置
     * @return 結果
     */
    int insertOrUpdate(SysTableConfigTemplate template);
}
