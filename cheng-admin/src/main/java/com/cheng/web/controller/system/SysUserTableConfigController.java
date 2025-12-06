package com.cheng.web.controller.system;

import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.system.service.ISysUserTableConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 使用者表格欄位配置Controller
 *
 * @author cheng
 * @since 2025-12-06
 */
@Slf4j
@RestController
@RequestMapping("/system/tableConfig")
@RequiredArgsConstructor
public class SysUserTableConfigController extends BaseController {

    private final ISysUserTableConfigService sysUserTableConfigService;

    /**
     * 取得指定頁面的欄位配置
     *
     * @param pageKey 頁面標識
     * @return 欄位配置（JSON字串）
     */
    @GetMapping("/{pageKey}")
    public AjaxResult getConfig(@PathVariable String pageKey) {
        try {
            String columnConfig = sysUserTableConfigService.getColumnConfig(pageKey);
            // 使用 AjaxResult.success().put("data", columnConfig) 確保數據在 data 欄位
            return AjaxResult.success().put("data", columnConfig);
        } catch (Exception e) {
            log.error("取得表格欄位配置失敗：pageKey={}, error={}", pageKey, e.getMessage(), e);
            return error("取得配置失敗：" + e.getMessage());
        }
    }

    /**
     * 儲存表格欄位配置
     *
     * @param request 包含 pageKey 和 columnConfig 的請求
     * @return 操作結果
     */
    @PostMapping
    public AjaxResult saveConfig(@RequestBody SaveConfigRequest request) {
        try {
            if (request.getPageKey() == null || request.getPageKey().trim().isEmpty()) {
                return error("頁面標識不能為空");
            }
            if (request.getColumnConfig() == null || request.getColumnConfig().trim().isEmpty()) {
                return error("欄位配置不能為空");
            }

            int result = sysUserTableConfigService.saveColumnConfig(
                    request.getPageKey(),
                    request.getColumnConfig()
            );

            return toAjax(result);
        } catch (Exception e) {
            log.error("儲存表格欄位配置失敗：pageKey={}, error={}",
                    request.getPageKey(), e.getMessage(), e);
            return error("儲存配置失敗：" + e.getMessage());
        }
    }

    /**
     * 儲存配置請求類別
     */
    @Data
    public static class SaveConfigRequest {
        private String pageKey;
        private String columnConfig;
    }
}
