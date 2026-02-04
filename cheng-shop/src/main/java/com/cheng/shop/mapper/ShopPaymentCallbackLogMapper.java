package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopPaymentCallbackLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 金流回調紀錄 Mapper
 *
 * @author cheng
 */
@Mapper
public interface ShopPaymentCallbackLogMapper {

    int insertLog(ShopPaymentCallbackLog log);

    java.util.List<ShopPaymentCallbackLog> selectLogList(ShopPaymentCallbackLog log);

    ShopPaymentCallbackLog selectLogById(Long logId);
}
