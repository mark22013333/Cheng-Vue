package com.cheng.web.controller.system;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.SysConfig;
import com.cheng.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 參數設定 訊息操作處理
 *
 * @author cheng
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {
    @Autowired
    private ISysConfigService configService;

    /**
     * 取得參數設定列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(SysConfig config) {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getDataTable(list);
    }

    @Log(title = "參數管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.EXPORT + "')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysConfig config) {
        List<SysConfig> list = configService.selectConfigList(config);
        ExcelUtil<SysConfig> util = new ExcelUtil<>(SysConfig.class);
        util.exportExcel(response, list, "參數數據");
    }

    /**
     * 根據參數編號取得詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.QUERY + "')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) {
        return success(configService.selectConfigById(configId));
    }

    /**
     * 根據參數鍵名查詢參數值
     */
    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@PathVariable String configKey) {
        return success(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增參數設定
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.ADD + "')")
    @Log(title = "參數管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return error("新增參數'" + config.getConfigName() + "'失敗，參數鍵名已存在");
        }
        config.setCreateBy(getUsername());
        return toAjax(configService.insertConfig(config));
    }

    /**
     * 修改參數設定
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.EDIT + "')")
    @Log(title = "參數管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return error("修改參數'" + config.getConfigName() + "'失敗，參數鍵名已存在");
        }
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateConfig(config));
    }

    /**
     * 刪除參數設定
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.REMOVE + "')")
    @Log(title = "參數管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return success();
    }

    /**
     * 重新整理參數暫存
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Config.REMOVE + "')")
    @Log(title = "參數管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public AjaxResult refreshCache() {
        configService.resetConfigCache();
        return success();
    }
}
