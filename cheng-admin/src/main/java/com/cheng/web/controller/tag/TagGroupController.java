package com.cheng.web.controller.tag;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.system.domain.SysTagGroup;
import com.cheng.system.service.ISysTagGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 標籤群組管理 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/tag/group")
@RequiredArgsConstructor
public class TagGroupController extends BaseController {

    private final ISysTagGroupService sysTagGroupService;

    // ==================== LINE 標籤群組 ====================

    /**
     * 查詢 LINE 標籤群組列表
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:list')")
    @GetMapping("/line/list")
    public TableDataInfo lineList(SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_LINE);
        startPage();
        List<SysTagGroup> list = sysTagGroupService.selectSysTagGroupList(sysTagGroup);
        return getDataTable(list);
    }

    /**
     * 取得 LINE 標籤群組詳情
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:query')")
    @GetMapping("/line/{groupId}")
    public AjaxResult lineGetInfo(@PathVariable Long groupId) {
        SysTagGroup group = sysTagGroupService.selectSysTagGroupById(groupId);
        if (group != null && !SysTagGroup.SCOPE_LINE.equals(group.getPlatformScope())) {
            return error("此群組不屬於 LINE 平台");
        }
        return success(group);
    }

    /**
     * 新增 LINE 標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:add')")
    @Log(title = "LINE 標籤群組", businessType = BusinessType.INSERT)
    @PostMapping("/line")
    public AjaxResult lineAdd(@Validated @RequestBody SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_LINE);
        sysTagGroup.setCreateBy(getUsername());
        return toAjax(sysTagGroupService.insertSysTagGroup(sysTagGroup));
    }

    /**
     * 修改 LINE 標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:edit')")
    @Log(title = "LINE 標籤群組", businessType = BusinessType.UPDATE)
    @PutMapping("/line")
    public AjaxResult lineEdit(@Validated @RequestBody SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_LINE);
        sysTagGroup.setUpdateBy(getUsername());
        return toAjax(sysTagGroupService.updateSysTagGroup(sysTagGroup));
    }

    /**
     * 刪除 LINE 標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:remove')")
    @Log(title = "LINE 標籤群組", businessType = BusinessType.DELETE)
    @DeleteMapping("/line/{groupIds}")
    public AjaxResult lineRemove(@PathVariable Long[] groupIds) {
        return toAjax(sysTagGroupService.deleteSysTagGroupByIds(groupIds));
    }

    /**
     * 執行 LINE 群組運算
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:calc')")
    @Log(title = "LINE 群組運算", businessType = BusinessType.OTHER)
    @PostMapping("/line/calc/{groupId}")
    public AjaxResult lineCalc(@PathVariable Long groupId) {
        Map<String, Object> result = sysTagGroupService.executeLineGroupCalc(groupId);
        return success(result);
    }

    /**
     * 預覽 LINE 群組運算結果
     */
    @PreAuthorize("@ss.hasPermi('tag:group:line:query')")
    @GetMapping("/line/preview/{groupId}")
    public AjaxResult linePreview(@PathVariable Long groupId,
                                  @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = sysTagGroupService.previewGroupCalc(groupId, limit);
        return success(result);
    }

    // ==================== 庫存標籤群組 ====================

    /**
     * 查詢庫存標籤群組列表
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:list')")
    @GetMapping("/inventory/list")
    public TableDataInfo inventoryList(SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_INVENTORY);
        startPage();
        List<SysTagGroup> list = sysTagGroupService.selectSysTagGroupList(sysTagGroup);
        return getDataTable(list);
    }

    /**
     * 取得庫存標籤群組詳情
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:query')")
    @GetMapping("/inventory/{groupId}")
    public AjaxResult inventoryGetInfo(@PathVariable Long groupId) {
        SysTagGroup group = sysTagGroupService.selectSysTagGroupById(groupId);
        if (group != null && !SysTagGroup.SCOPE_INVENTORY.equals(group.getPlatformScope())) {
            return error("此群組不屬於庫存平台");
        }
        return success(group);
    }

    /**
     * 新增庫存標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:add')")
    @Log(title = "庫存標籤群組", businessType = BusinessType.INSERT)
    @PostMapping("/inventory")
    public AjaxResult inventoryAdd(@Validated @RequestBody SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_INVENTORY);
        sysTagGroup.setCreateBy(getUsername());
        return toAjax(sysTagGroupService.insertSysTagGroup(sysTagGroup));
    }

    /**
     * 修改庫存標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:edit')")
    @Log(title = "庫存標籤群組", businessType = BusinessType.UPDATE)
    @PutMapping("/inventory")
    public AjaxResult inventoryEdit(@Validated @RequestBody SysTagGroup sysTagGroup) {
        sysTagGroup.setPlatformScope(SysTagGroup.SCOPE_INVENTORY);
        sysTagGroup.setUpdateBy(getUsername());
        return toAjax(sysTagGroupService.updateSysTagGroup(sysTagGroup));
    }

    /**
     * 刪除庫存標籤群組
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:remove')")
    @Log(title = "庫存標籤群組", businessType = BusinessType.DELETE)
    @DeleteMapping("/inventory/{groupIds}")
    public AjaxResult inventoryRemove(@PathVariable Long[] groupIds) {
        return toAjax(sysTagGroupService.deleteSysTagGroupByIds(groupIds));
    }

    /**
     * 執行庫存群組運算
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:calc')")
    @Log(title = "庫存群組運算", businessType = BusinessType.OTHER)
    @PostMapping("/inventory/calc/{groupId}")
    public AjaxResult inventoryCalc(@PathVariable Long groupId) {
        Map<String, Object> result = sysTagGroupService.executeInventoryGroupCalc(groupId);
        return success(result);
    }

    /**
     * 預覽庫存群組運算結果
     */
    @PreAuthorize("@ss.hasPermi('tag:group:inventory:query')")
    @GetMapping("/inventory/preview/{groupId}")
    public AjaxResult inventoryPreview(@PathVariable Long groupId,
                                       @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = sysTagGroupService.previewGroupCalc(groupId, limit);
        return success(result);
    }
}
