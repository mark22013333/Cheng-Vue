package com.cheng.shop.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.shop.domain.ShopPaymentCallbackLog;
import com.cheng.shop.mapper.ShopPaymentCallbackLogMapper;
import com.cheng.shop.service.IShopPaymentCallbackLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 金流回調紀錄 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopPaymentCallbackLogServiceImpl implements IShopPaymentCallbackLogService {

    private final ShopPaymentCallbackLogMapper logMapper;

    @Override
    public void logCallback(ShopPaymentCallbackLog scLog) {
        if (scLog == null) {
            return;
        }
        if (scLog.getCreateTime() == null) {
            scLog.setCreateTime(DateUtils.getNowDate());
        }
        try {
            logMapper.insertLog(scLog);
        } catch (Exception e) {
            log.warn("金流回調紀錄寫入失敗", e);
        }
    }

    @Override
    public List<ShopPaymentCallbackLog> selectLogList(ShopPaymentCallbackLog log) {
        return logMapper.selectLogList(log);
    }

    @Override
    public ShopPaymentCallbackLog selectLogById(Long logId) {
        return logMapper.selectLogById(logId);
    }
}
