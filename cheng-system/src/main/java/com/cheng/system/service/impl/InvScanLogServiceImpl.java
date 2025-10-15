package com.cheng.system.service.impl;

import com.cheng.system.domain.InvScanLog;
import com.cheng.system.mapper.InvScanLogMapper;
import com.cheng.system.service.IInvScanLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 掃描日誌 服務層實現
 *
 * @author cheng
 */
@Service
public class InvScanLogServiceImpl implements IInvScanLogService {

    @Autowired
    private InvScanLogMapper invScanLogMapper;

    @Override
    public List<InvScanLog> selectInvScanLogList(InvScanLog invScanLog) {
        return invScanLogMapper.selectInvScanLogList(invScanLog);
    }

    @Override
    public int insertInvScanLog(InvScanLog log) {
        return invScanLogMapper.insertInvScanLog(log);
    }
}
