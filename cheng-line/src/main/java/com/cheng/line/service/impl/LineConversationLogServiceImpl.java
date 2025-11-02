package com.cheng.line.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.line.domain.LineConversationLog;
import com.cheng.line.mapper.LineConversationLogMapper;
import com.cheng.line.service.ILineConversationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LINE 對話記錄 服務層實作
 *
 * @author cheng
 */
@Service
public class LineConversationLogServiceImpl implements ILineConversationLogService {

    @Autowired
    private LineConversationLogMapper lineConversationLogMapper;

    @Override
    public LineConversationLog selectLineConversationLogById(Long id) {
        return lineConversationLogMapper.selectLineConversationLogById(id);
    }

    @Override
    public List<LineConversationLog> selectLineConversationLogList(LineConversationLog lineConversationLog) {
        return lineConversationLogMapper.selectLineConversationLogList(lineConversationLog);
    }

    @Override
    public List<LineConversationLog> selectRecentConversations(String lineUserId, int limit) {
        return lineConversationLogMapper.selectRecentConversations(lineUserId, limit);
    }

    @Override
    public int insertLineConversationLog(LineConversationLog lineConversationLog) {
        return lineConversationLogMapper.insertLineConversationLog(lineConversationLog);
    }

    @Override
    public int batchInsertLineConversationLog(List<LineConversationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return 0;
        }
        return lineConversationLogMapper.batchInsertLineConversationLog(logs);
    }

    @Override
    public int deleteLineConversationLogById(Long id) {
        return lineConversationLogMapper.deleteLineConversationLogById(id);
    }

    @Override
    public int deleteLineConversationLogByIds(Long[] ids) {
        return lineConversationLogMapper.deleteLineConversationLogByIds(ids);
    }

    @Override
    public int deleteLineConversationLogByLineUserId(String lineUserId) {
        return lineConversationLogMapper.deleteLineConversationLogByLineUserId(lineUserId);
    }

    @Override
    public int deleteLineConversationLogBeforeDays(int days) {
        String beforeDate = DateUtils.getBeforeDaysDate(days);
        return lineConversationLogMapper.deleteLineConversationLogBeforeDate(beforeDate);
    }
}
