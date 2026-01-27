package com.cheng.shop.service;

import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.system.service.IInvStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商城庫存同步服務
 * <p>
 * 負責將商城訂單的庫存變動同步到庫存管理系統
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopStockSyncService {

    private final IInvStockService invStockService;

    /**
     * 訂單建立時扣減庫存物品
     *
     * @param orderItems 訂單明細
     * @param operatorId 操作人員ID
     * @param orderNo    訂單編號
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductStockForOrder(List<ShopOrderItem> orderItems, Long operatorId, String orderNo) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        for (ShopOrderItem item : orderItems) {
            if (item.getInvItemId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                try {
                    int result = invStockService.stockOut(
                            item.getInvItemId(),
                            item.getQuantity(),
                            operatorId,
                            "商城訂單扣減：" + orderNo
                    );
                    if (result > 0) {
                        log.info("庫存同步：扣減庫存物品 itemId={}, 數量={}, 訂單={}",
                                item.getInvItemId(), item.getQuantity(), orderNo);
                    } else {
                        log.warn("庫存同步：扣減庫存物品失敗，可能庫存不足 itemId={}, 訂單={}",
                                item.getInvItemId(), orderNo);
                    }
                } catch (Exception e) {
                    log.error("庫存同步：扣減庫存物品異常 itemId={}, 訂單={}, 錯誤={}",
                            item.getInvItemId(), orderNo, e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * 訂單取消/退款時恢復庫存物品
     *
     * @param orderItems 訂單明細
     * @param operatorId 操作人員ID
     * @param orderNo    訂單編號
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreStockForOrder(List<ShopOrderItem> orderItems, Long operatorId, String orderNo) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        for (ShopOrderItem item : orderItems) {
            if (item.getInvItemId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                try {
                    int result = invStockService.stockIn(
                            item.getInvItemId(),
                            item.getQuantity(),
                            operatorId,
                            "商城訂單恢復：" + orderNo
                    );
                    if (result > 0) {
                        log.info("庫存同步：恢復庫存物品 itemId={}, 數量={}, 訂單={}",
                                item.getInvItemId(), item.getQuantity(), orderNo);
                    }
                } catch (Exception e) {
                    log.error("庫存同步：恢復庫存物品異常 itemId={}, 訂單={}, 錯誤={}",
                            item.getInvItemId(), orderNo, e.getMessage());
                    throw e;
                }
            }
        }
    }
}
