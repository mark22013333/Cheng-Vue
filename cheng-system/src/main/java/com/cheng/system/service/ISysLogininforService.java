package com.cheng.system.service;

import com.cheng.system.domain.SysLogininfor;

import java.util.List;

/**
 * 系統訪問日誌情況訊息 服務層
 *
 * @author cheng
 */
public interface ISysLogininforService
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
     * 批次刪除系統登入日誌
     *
     * @param infoIds 需要刪除的登入日誌ID
     * @return 結果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清除系統登入日誌
     */
    public void cleanLogininfor();
}
