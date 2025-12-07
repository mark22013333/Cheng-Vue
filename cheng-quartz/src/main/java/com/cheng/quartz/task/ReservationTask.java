package com.cheng.quartz.task;

import com.cheng.common.utils.DateUtils;
import com.cheng.quartz.enums.ReserveStatus;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.mapper.InvBorrowMapper;
import com.cheng.system.service.IInvItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 預約相關定時任務
 * <p>
 * 用於 Quartz 排程系統，支援從後台介面設定參數
 * <p>
 * <b>Quartz 設定範例：</b>
 * <pre>
 * Bean名稱: reservationTask
 * 方法名稱: cancelExpiredReservations
 * 參數: (無需參數)
 * </pre>
 *
 * @author cheng
 */
@Slf4j
@Component("reservationTask")
@RequiredArgsConstructor
public class ReservationTask {

    private final InvBorrowMapper invBorrowMapper;
    private final IInvItemService invItemService;

    /**
     * 自動取消過期預約
     * <p>
     * 查詢所有超過結束日期且仍為待審核狀態的預約，自動取消並恢復物品數量
     * <p>
     * <b>執行頻率建議：</b>每小時執行一次
     *
     * @param params Quartz 參數（可選，JSON 格式），目前未使用
     */
    public void cancelExpiredReservations(String params) {
        try {
            log.info("開始執行過期預約取消任務");

            // 查詢所有過期的待審核預約
            List<InvBorrow> expiredReservations = invBorrowMapper.selectExpiredReservations();

            if (expiredReservations.isEmpty()) {
                log.info("沒有過期的預約需要處理");
                return;
            }

            int cancelledCount = 0;
            int restoredCount = 0;

            for (InvBorrow reservation : expiredReservations) {
                try {
                    // 更新預約狀態為已取消
                    reservation.setReserveStatus(ReserveStatus.CANCELLED.getCode());
                    reservation.setUpdateBy("system");
                    reservation.setUpdateTime(DateUtils.getNowDate());
                    reservation.setRemark("系統自動取消：預約已過期");

                    invBorrowMapper.updateInvBorrow(reservation);

                    // 恢復物品的預約數量
                    boolean restored = invItemService.restoreReservedQuantity(
                            reservation.getItemId(),
                            reservation.getQuantity()
                    );

                    if (restored) {
                        restoredCount++;
                        log.info("已恢復物品預約數量 - itemId: {}, quantity: {}",
                                reservation.getItemId(), reservation.getQuantity());
                    }

                    cancelledCount++;
                    log.info("已取消過期預約 - borrowId: {}, itemId: {}, userId: {}",
                            reservation.getBorrowId(), reservation.getItemId(), reservation.getBorrowerId());

                } catch (Exception e) {
                    log.error("取消過期預約失敗 - borrowId: {}", reservation.getBorrowId(), e);
                }
            }

            log.info("過期預約取消任務完成 - 共取消 {} 個預約，恢復 {} 個物品數量",
                    cancelledCount, restoredCount);

        } catch (Exception e) {
            log.error("執行過期預約取消任務失敗", e);
        }
    }
}
