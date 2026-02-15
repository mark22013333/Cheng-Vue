package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.shop.domain.ShopCvsStoreTemp;
import com.cheng.shop.logistics.IShopLogisticsService;
import com.cheng.shop.logistics.ShippingMethodVO;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流 API 控制器
 *
 * @author cheng
 */
@Slf4j
@PublicApi
@RestController
@RequestMapping("/shop/logistics")
@RequiredArgsConstructor
public class ShopLogisticsController extends BaseController {

    private final IShopLogisticsService logisticsService;

    /**
     * 查詢可用物流方式
     *
     * @param productAmount 商品金額（用於計算運費）
     */
    @GetMapping("/methods")
    public AjaxResult listMethods(@RequestParam(required = false, defaultValue = "0") BigDecimal productAmount) {
        List<ShippingMethodVO> methods = logisticsService.getAvailableMethods(productAmount);
        return success(methods);
    }

    /**
     * 取得電子地圖表單 HTML
     */
    @PostMapping("/map/url")
    public AjaxResult getMapUrl(@RequestBody MapUrlRequest request) {
        Long memberId = ShopMemberSecurityUtils.getMemberId();

        if (request.getShippingMethod() == null || request.getShippingMethod().isBlank()) {
            return error("請選擇物流方式");
        }
        if (request.getStoreKey() == null || request.getStoreKey().isBlank()) {
            return error("缺少 storeKey");
        }

        try {
            String formHtml = logisticsService.generateMapFormHtml(
                    request.getShippingMethod(),
                    request.getStoreKey(),
                    memberId
            );

            Map<String, String> data = new HashMap<>();
            data.put("formHtml", formHtml);
            return success(data);
        } catch (Exception e) {
            log.error("產生電子地圖 URL 失敗", e);
            return error("無法開啟門市地圖：" + e.getMessage());
        }
    }

    /**
     * 電子地圖回調（接收門市選取結果）
     * <p>
     * 此端點由綠界伺服器呼叫，不需要認證。
     * 回傳 HTML 頁面讓用戶看到選取結果，並嘗試自動關閉視窗。
     */
    @Anonymous
    @PostMapping(value = "/cvs/store/callback", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String cvsStoreCallback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("收到電子地圖回調：{}", params);

        String storeName = params.getOrDefault("CVSStoreName", "");
        String storeAddress = params.getOrDefault("CVSAddress", "");

        try {
            logisticsService.saveCvsStore(params);
            // 返回成功頁面，顯示門市資訊並自動關閉視窗
            return buildCallbackHtml(true, storeName, storeAddress, null);
        } catch (Exception e) {
            log.error("電子地圖回調處理失敗", e);
            return buildCallbackHtml(false, storeName, storeAddress, e.getMessage());
        }
    }

    /**
     * 建立回調 HTML 頁面
     */
    private String buildCallbackHtml(boolean success, String storeName, String storeAddress, String errorMsg) {
        String title = success ? "門市選取成功" : "門市選取失敗";
        String iconColor = success ? "#67c23a" : "#f56c6c";
        String icon = success ? "&#10004;" : "&#10006;";
        String message = success
                ? String.format("已選取門市：<strong>%s</strong><br><small>%s</small>", storeName, storeAddress)
                : String.format("選取失敗：%s", errorMsg != null ? errorMsg : "未知錯誤");

        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                    }
                    .card {
                        background: white;
                        border-radius: 16px;
                        padding: 40px;
                        text-align: center;
                        max-width: 400px;
                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                    }
                    .icon {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%%;
                        background: %s;
                        color: white;
                        font-size: 40px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 24px;
                    }
                    h1 { font-size: 24px; color: #303133; margin-bottom: 16px; }
                    .message { font-size: 16px; color: #606266; line-height: 1.6; margin-bottom: 24px; }
                    .message strong { color: #303133; }
                    .message small { color: #909399; }
                    .hint { font-size: 14px; color: #909399; }
                    .countdown { font-weight: bold; color: #409eff; }
                    .btn {
                        display: inline-block;
                        margin-top: 20px;
                        padding: 12px 32px;
                        background: #409eff;
                        color: white;
                        border: none;
                        border-radius: 8px;
                        font-size: 16px;
                        cursor: pointer;
                        text-decoration: none;
                    }
                    .btn:hover { background: #66b1ff; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">%s</div>
                    <h1>%s</h1>
                    <div class="message">%s</div>
                    <p class="hint">視窗將在 <span class="countdown" id="countdown">3</span> 秒後自動關閉</p>
                    <button class="btn" onclick="window.close()">立即關閉</button>
                </div>
                <script>
                    let seconds = 3;
                    const countdownEl = document.getElementById('countdown');
                    const timer = setInterval(() => {
                        seconds--;
                        countdownEl.textContent = seconds;
                        if (seconds <= 0) {
                            clearInterval(timer);
                            window.close();
                            // 如果無法自動關閉，更新提示文字
                            setTimeout(() => {
                                document.querySelector('.hint').textContent = '請手動關閉此視窗，返回結帳頁面';
                            }, 500);
                        }
                    }, 1000);
                </script>
            </body>
            </html>
            """.formatted(title, iconColor, icon, title, message);
    }

    /**
     * 查詢超商門市暫存資訊
     */
    @GetMapping("/cvs/store/{storeKey}")
    public AjaxResult getCvsStore(@PathVariable String storeKey) {
        Long memberId = ShopMemberSecurityUtils.getMemberId();

        ShopCvsStoreTemp store = logisticsService.getCvsStore(storeKey, memberId);
        if (store == null) {
            return success(null);
        }

        // 轉換為前端需要的格式
        Map<String, Object> data = new HashMap<>();
        data.put("storeKey", store.getStoreKey());
        data.put("storeId", store.getStoreId());
        data.put("storeName", store.getStoreName());
        data.put("storeAddress", store.getStoreAddress());
        data.put("storeTel", store.getStoreTel());
        data.put("logisticsSubType", store.getLogisticsSub());
        data.put("expireTime", store.getExpireTime());

        return success(data);
    }

    /**
     * 物流狀態回調（接收綠界物流狀態更新通知）
     * <p>
     * 此端點由綠界伺服器呼叫，用於通知物流狀態變更（如已出貨、已送達等）。
     * 對應系統設定：shop.ecpay.logistics.server_reply_url
     */
    @Anonymous
    @PostMapping("/status-callback")
    public String logisticsStatusCallback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("收到綠界物流狀態回調：{}", params);

        try {
            // 處理物流狀態更新
            logisticsService.handleStatusCallback(params);
            // 綠界要求回傳 "1|OK" 表示成功
            return "1|OK";
        } catch (Exception e) {
            log.error("物流狀態回調處理失敗", e);
            // 回傳錯誤訊息
            return "0|" + e.getMessage();
        }
    }

    /**
     * 計算運費
     */
    @GetMapping("/shipping-fee")
    public AjaxResult calculateShippingFee(
            @RequestParam String shippingMethod,
            @RequestParam(required = false, defaultValue = "0") BigDecimal productAmount) {
        try {
            BigDecimal fee = logisticsService.calculateShippingFee(shippingMethod, productAmount);
            return success(fee);
        } catch (Exception e) {
            log.error("計算運費失敗", e);
            return error("無效的物流方式");
        }
    }

    /**
     * 從 HttpServletRequest 提取所有參數
     */
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    /**
     * 電子地圖 URL 請求
     */
    @lombok.Data
    public static class MapUrlRequest {
        private String shippingMethod;
        private String storeKey;
    }
}
