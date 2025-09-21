package com.cheng.system.mapper;

import com.cheng.system.domain.SysLogininfor;

import java.util.List;

/**
 * 系統訪問日誌情況訊息 數據層
 *
 * @author cheng
 */
public interface SysLogininforMapper
{
    /**
     * 新增系統登入日誌
     *
     * @param logininfor 訪問日誌物件
     */
    public void insertLogininfor(SysLogininfor logininfor);

    /**
     * 查詢系統登入日誌集合
     *
     * @param logininfor 訪問日誌物件
     * @return 登入記錄集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量刪除系統登入日誌
     *
     * @param infoIds 需要刪除的登入日誌ID
     * @return 結果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清除系統登入日誌
     *
     * @return 結果
     */
    public int cleanLogininfor();
}
