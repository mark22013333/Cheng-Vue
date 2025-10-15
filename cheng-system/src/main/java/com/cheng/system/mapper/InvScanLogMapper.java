package com.cheng.system.mapper;

import com.cheng.system.domain.InvScanLog;
import java.util.List;

public interface InvScanLogMapper {
    /**
     * 查詢掃描記錄列表
     */
    List<InvScanLog> selectInvScanLogList(InvScanLog invScanLog);
    
    /**
     * 新增掃描記錄
     */
    int insertInvScanLog(InvScanLog log);
}
