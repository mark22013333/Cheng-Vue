package com.cheng.line.service.impl;

import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.mapper.LineMessageLogMapper;
import com.cheng.line.service.ILineMessageLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LINE 推播訊息記錄 服務層實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineMessageLogServiceImpl implements ILineMessageLogService {

    private @Resource LineMessageLogMapper lineMessageLogMapper;

    @Override
    public LineMessageLog selectLineMessageLogById(Long messageId) {
        return lineMessageLogMapper.selectLineMessageLogById(messageId);
    }

    @Override
    public List<LineMessageLog> selectLineMessageLogList(LineMessageLog lineMessageLog) {
        return lineMessageLogMapper.selectLineMessageLogList(lineMessageLog);
    }

    @Override
    public List<LineMessageLog> selectMessageLogByUserId(String lineUserId) {
        return lineMessageLogMapper.selectMessageLogByUserId(lineUserId);
    }

    @Override
    public List<LineMessageLog> selectMessageLogByTagId(Long tagId) {
        return lineMessageLogMapper.selectMessageLogByTagId(tagId);
    }

    @Override
    public int insertLineMessageLog(LineMessageLog lineMessageLog) {
        return lineMessageLogMapper.insertLineMessageLog(lineMessageLog);
    }

    @Override
    public int updateLineMessageLog(LineMessageLog lineMessageLog) {
        return lineMessageLogMapper.updateLineMessageLog(lineMessageLog);
    }

    @Override
    public int deleteLineMessageLogById(Long messageId) {
        return lineMessageLogMapper.deleteLineMessageLogById(messageId);
    }

    @Override
    public int deleteLineMessageLogByIds(Long[] messageIds) {
        return lineMessageLogMapper.deleteLineMessageLogByIds(messageIds);
    }

    @Override
    public int countSuccessMessagesByConfigId(Integer configId) {
        return lineMessageLogMapper.countSuccessMessagesByConfigId(configId);
    }

    @Override
    public int countFailedMessagesByConfigId(Integer configId) {
        return lineMessageLogMapper.countFailedMessagesByConfigId(configId);
    }
}
