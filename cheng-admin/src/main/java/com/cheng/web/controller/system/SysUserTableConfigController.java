package com.cheng.web.controller.system;

import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.system.service.ISysUserTableConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * 取得有效的欄位配置（智慧判斷：個人配置 or 模版）
     * 不需要特定權限 — 任何登入使用者都可以讀取
     */
    @GetMapping("/{pageKey}")
    public AjaxResult getConfig(@PathVariable String pageKey) {
        try {
            String columnConfig = sysUserTableConfigService.getEffectiveConfig(pageKey);
            return AjaxResult.success().put("data", columnConfig);
        } catch (Exception e) {
            log.error("取得表格欄位配置失敗：pageKey={}, error={}", pageKey, e.getMessage(), e);
            return error("取得配置失敗：" + e.getMessage());
        }
    }

    /**
     * 儲存使用者個人欄位配置
     * 需要 system:tableConfig:customize 權限
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.TableConfig.CUSTOMIZE + "')")
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
     * 取得模版配置
     * 需要 system:tableConfig:template 權限
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.TableConfig.TEMPLATE + "')")
    @GetMapping("/template/{pageKey}")
    public AjaxResult getTemplateConfig(@PathVariable String pageKey) {
        try {
            String config = sysUserTableConfigService.getTemplateConfig(pageKey);
            return AjaxResult.success().put("data", config);
        } catch (Exception e) {
            log.error("取得模版配置失敗：pageKey={}, error={}", pageKey, e.getMessage(), e);
            return error("取得模版配置失敗：" + e.getMessage());
        }
    }

    /**
     * 儲存模版配置
     * 需要 system:tableConfig:template 權限
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.TableConfig.TEMPLATE + "')")
    @PostMapping("/template")
    public AjaxResult saveTemplateConfig(@RequestBody SaveConfigRequest request) {
        try {
            if (request.getPageKey() == null || request.getPageKey().trim().isEmpty()) {
                return error("頁面標識不能為空");
            }
            if (request.getColumnConfig() == null || request.getColumnConfig().trim().isEmpty()) {
                return error("欄位配置不能為空");
            }

            int result = sysUserTableConfigService.saveTemplateConfig(
                    request.getPageKey(),
                    request.getColumnConfig()
            );

            return toAjax(result);
        } catch (Exception e) {
            log.error("儲存模版配置失敗：pageKey={}, error={}",
                    request.getPageKey(), e.getMessage(), e);
            return error("儲存模版配置失敗：" + e.getMessage());
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
