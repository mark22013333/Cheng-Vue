package com.cheng.shop.logistics.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopCvsStoreTemp;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.enums.OrderStatus;
import com.cheng.shop.enums.ShipStatus;
import com.cheng.shop.enums.ShippingMethod;
import com.cheng.shop.logistics.EcpayLogisticsGateway;
import com.cheng.shop.logistics.IShopLogisticsService;
import com.cheng.shop.logistics.ShippingMethodVO;
import com.cheng.shop.mapper.ShopCvsStoreTempMapper;
import com.cheng.shop.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 物流服務實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopLogisticsServiceImpl implements IShopLogisticsService {

    private final ShopConfigService shopConfig;
    private final ShopCvsStoreTempMapper cvsStoreTempMapper;
    private final ShopOrderMapper orderMapper;
    private final EcpayLogisticsGateway logisticsGateway;

    @Override
    public List<ShippingMethodVO> getAvailableMethods(BigDecimal productAmount) {
        String methodsConfig = shopConfig.getLogisticsMethods();

        BigDecimal homeDeliveryFee = shopConfig.getHomeDeliveryFee();
        BigDecimal homeDeliveryFreeThreshold = shopConfig.getHomeDeliveryFreeThreshold();
        BigDecimal cvsFee = shopConfig.getCvsFee();
        BigDecimal cvsFreeThreshold = shopConfig.getCvsFreeThreshold();

        List<ShippingMethodVO> methods = new ArrayList<>();
        String[] codes = methodsConfig.split(",");

        for (String code : codes) {
            code = code.trim();
            if (code.isEmpty()) {
                continue;
            }

            try {
                ShippingMethod method = ShippingMethod.fromCode(code);
                BigDecimal fee;
                BigDecimal freeThreshold;

                if (method.isCvs()) {
                    fee = cvsFee;
                    freeThreshold = cvsFreeThreshold;
                } else {
                    fee = homeDeliveryFee;
                    freeThreshold = homeDeliveryFreeThreshold;
                }

                // 計算實際運費（考慮免運門檻）
                BigDecimal actualFee = calculateActualFee(fee, freeThreshold, productAmount);

                methods.add(ShippingMethodVO.builder()
                        .code(method.getCode())
                        .name(method.getDescription())
                        .description(getMethodDescription(method))
                        .fee(actualFee)
                        .freeThreshold(freeThreshold)
                        .requireAddress(!method.isCvs())
                        .requireCvsStore(method.isCvs())
                        .build());
            } catch (Exception e) {
                log.warn("無效的物流方式代碼：{}", code);
            }
        }

        return methods;
    }

    @Override
    public String generateMapFormHtml(String shippingMethod, String storeKey, Long memberId) {
        ShippingMethod method = ShippingMethod.fromCode(shippingMethod);
        if (!method.isCvs()) {
            throw new ServiceException("物流方式不支援電子地圖：" + shippingMethod);
        }

        String logisticsSubType = method.getEcpayLogisticsSubType();
        String callbackBaseUrl = shopConfig.getLogisticsCallbackBaseUrl();

        String serverReplyUrl = callbackBaseUrl.endsWith("/")
                ? callbackBaseUrl + "shop/logistics/cvs/store/callback"
                : callbackBaseUrl + "/shop/logistics/cvs/store/callback";

        // 預先建立暫存記錄（儲存 memberId 以便驗證）
        createPendingCvsStore(storeKey, memberId, logisticsSubType);

        return logisticsGateway.generateMapFormHtml(storeKey, logisticsSubType, serverReplyUrl);
    }

    @Override
    @Transactional
    public void saveCvsStore(Map<String, String> params) {
        // 驗證 MerchantID
        if (!logisticsGateway.verifyMapCallback(params)) {
            throw new ServiceException("電子地圖回調驗證失敗");
        }

        // ExtraData 中帶有 storeKey
        String storeKey = params.get("ExtraData");
        if (storeKey == null || storeKey.isBlank()) {
            log.warn("電子地圖回調缺少 ExtraData（storeKey）");
            throw new ServiceException("缺少 storeKey");
        }

        // 查詢預先建立的暫存記錄
        ShopCvsStoreTemp existing = cvsStoreTempMapper.selectByStoreKey(storeKey);
        if (existing == null) {
            log.warn("找不到對應的門市暫存記錄：storeKey={}", storeKey);
            throw new ServiceException("找不到對應的暫存記錄");
        }

        // 更新門市資訊
        existing.setStoreId(params.get("CVSStoreID"));
        existing.setStoreName(params.get("CVSStoreName"));
        existing.setStoreAddress(params.get("CVSAddress"));
        existing.setStoreTel(params.get("CVSTelephone"));
        existing.setCvsOutside(params.get("CVSOutSide"));

        // 重設過期時間
        int expireMinutes = shopConfig.getCvsStoreExpireMinutes();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expireMinutes);
        existing.setExpireTime(cal.getTime());

        cvsStoreTempMapper.updateByStoreKey(existing);

        log.info("超商門市選取成功：storeKey={}, storeId={}, storeName={}",
                storeKey, existing.getStoreId(), existing.getStoreName());
    }

    @Override
    public ShopCvsStoreTemp getCvsStore(String storeKey, Long memberId) {
        ShopCvsStoreTemp store = cvsStoreTempMapper.selectByStoreKeyAndMemberId(storeKey, memberId);
        if (store == null) {
            return null;
        }

        // 檢查是否已選取門市（storeId 有值）
        if (store.getStoreId() == null || store.getStoreId().isBlank()) {
            return null;
        }

        // 檢查是否過期
        if (store.getExpireTime() != null && store.getExpireTime().before(new Date())) {
            log.debug("門市暫存已過期：storeKey={}", storeKey);
            return null;
        }

        return store;
    }

    @Override
    public void deleteCvsStore(String storeKey) {
        cvsStoreTempMapper.deleteByStoreKey(storeKey);
    }

    @Override
    public int cleanExpiredStores() {
        int deleted = cvsStoreTempMapper.deleteExpired();
        if (deleted > 0) {
            log.info("清除過期門市暫存記錄：{} 筆", deleted);
        }
        return deleted;
    }

    @Override
    public BigDecimal calculateShippingFee(String shippingMethod, BigDecimal productAmount) {
        ShippingMethod method = ShippingMethod.fromCode(shippingMethod);

        BigDecimal fee;
        BigDecimal freeThreshold;

        if (method.isCvs()) {
            fee = shopConfig.getCvsFee();
            freeThreshold = shopConfig.getCvsFreeThreshold();
        } else {
            fee = shopConfig.getHomeDeliveryFee();
            freeThreshold = shopConfig.getHomeDeliveryFreeThreshold();
        }

        return calculateActualFee(fee, freeThreshold, productAmount);
    }

    @Override
    @Transactional
    public void handleStatusCallback(Map<String, String> params) {
        // 解析綠界物流狀態回調參數
        String allPayLogisticsId = params.get("AllPayLogisticsID");
        String rtnCode = params.get("RtnCode");
        String rtnMsg = params.get("RtnMsg");
        String updateStatusDate = params.get("UpdateStatusDate");

        log.info("物流狀態更新：AllPayLogisticsID={}, RtnCode={}, RtnMsg={}, UpdateStatusDate={}",
                allPayLogisticsId, rtnCode, rtnMsg, updateStatusDate);

        if (allPayLogisticsId == null || allPayLogisticsId.isBlank()) {
            log.warn("物流狀態回調缺少 AllPayLogisticsID");
            return;
        }

        // 根據 AllPayLogisticsID（即 shippingNo）查詢訂單
        ShopOrder order = orderMapper.selectOrderByShippingNo(allPayLogisticsId);
        if (order == null) {
            log.warn("找不到對應訂單：AllPayLogisticsID={}", allPayLogisticsId);
            return;
        }

        // 根據 RtnCode 決定新的物流狀態
        ShipStatus newShipStatus = mapEcpayRtnCodeToShipStatus(rtnCode);
        if (newShipStatus == null) {
            log.info("未處理的物流狀態碼：RtnCode={}, RtnMsg={}", rtnCode, rtnMsg);
            return;
        }

        ShipStatus currentStatus = order.getShipStatusEnum();
        if (currentStatus == newShipStatus) {
            log.debug("物流狀態未變更：orderId={}, shipStatus={}", order.getOrderId(), currentStatus);
            return;
        }

        // 更新物流狀態
        orderMapper.updateShipStatus(order.getOrderId(), newShipStatus.getCode());
        log.info("訂單物流狀態已更新：orderId={}, orderNo={}, {} -> {}",
                order.getOrderId(), order.getOrderNo(), currentStatus, newShipStatus);

        // 如果配達完成，同時更新訂單狀態為「已完成」
        if (newShipStatus == ShipStatus.DELIVERED) {
            orderMapper.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED.getCode());
            log.info("訂單已完成：orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());
        }
    }

    /**
     * 將綠界物流狀態碼對應到系統物流狀態
     * <p>
     * ECPay 物流狀態碼說明：
     * <ul>
     *   <li>300: 訂單處理中（貨態：已取件）</li>
     *   <li>2030, 3024: 配達完成（買家已取貨）</li>
     *   <li>2063, 2067: 退貨完成</li>
     *   <li>2068, 2069: 遺失賠償處理中</li>
     *   <li>3018, 3020, 3022: 運送中（轉運中、抵達門市、等待取貨）</li>
     * </ul>
     *
     * @param rtnCode ECPay RtnCode
     * @return 對應的物流狀態，若無法對應則回傳 null
     */
    private ShipStatus mapEcpayRtnCodeToShipStatus(String rtnCode) {
        if (rtnCode == null || rtnCode.isBlank()) {
            return null;
        }

        return switch (rtnCode) {
            // 已取件（物流商已收貨）
            case "300" -> ShipStatus.SHIPPED;
            // 轉運中、運送中
            case "3018", "3020" -> ShipStatus.IN_TRANSIT;
            // 抵達門市、等待取貨
            case "3022", "3023" -> ShipStatus.IN_TRANSIT;
            // 配達完成（買家已取貨）
            case "2030", "3024" -> ShipStatus.DELIVERED;
            // 退貨完成
            case "2063", "2067" -> ShipStatus.RETURNED;
            // 其他狀態暫不處理
            default -> null;
        };
    }

    // ==================== 內部方法 ====================

    /**
     * 預先建立門市暫存記錄（用於關聯 memberId）
     */
    private void createPendingCvsStore(String storeKey, Long memberId, String logisticsSub) {
        // 先刪除舊的（如果存在）
        cvsStoreTempMapper.deleteByStoreKey(storeKey);

        int expireMinutes = shopConfig.getCvsStoreExpireMinutes();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expireMinutes);

        ShopCvsStoreTemp temp = new ShopCvsStoreTemp();
        temp.setStoreKey(storeKey);
        temp.setMemberId(memberId);
        temp.setLogisticsSub(logisticsSub);
        temp.setStoreId("");  // 待填入
        temp.setStoreName("");  // 待填入
        temp.setExpireTime(cal.getTime());
        temp.setCreateTime(new Date());

        cvsStoreTempMapper.insert(temp);
    }

    /**
     * 計算實際運費
     */
    private BigDecimal calculateActualFee(BigDecimal baseFee, BigDecimal freeThreshold, BigDecimal productAmount) {
        if (freeThreshold.compareTo(BigDecimal.ZERO) > 0
                && productAmount != null
                && productAmount.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return baseFee;
    }

    /**
     * 取得物流方式描述
     */
    private String getMethodDescription(ShippingMethod method) {
        return switch (method) {
            case HOME_DELIVERY -> "黑貓宅配，3-5 個工作天送達";
            case CVS_711 -> "7-ELEVEN 超商取貨，2-3 個工作天";
            case CVS_FAMILY -> "全家便利商店取貨，2-3 個工作天";
            case CVS_HILIFE -> "萊爾富便利商店取貨，2-3 個工作天";
            case STORE_PICKUP -> "門市自取";
        };
    }

}
