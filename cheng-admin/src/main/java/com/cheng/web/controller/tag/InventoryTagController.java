package com.cheng.web.controller.tag;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.system.domain.InvItemTagRelation;
import com.cheng.system.domain.SysTag;
import com.cheng.system.service.IInvItemTagRelationService;
import com.cheng.system.service.ISysTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 庫存標籤管理 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/tag/inventory")
@RequiredArgsConstructor
public class InventoryTagController extends BaseController {

    private final ISysTagService sysTagService;
    private final IInvItemTagRelationService invItemTagRelationService;

    // ==================== 標籤定義 CRUD ====================

    /**
     * 查詢庫存標籤列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(SysTag sysTag) {
        startPage();
        // 設定平台範圍為庫存
        sysTag.setPlatformScope(SysTag.SCOPE_INVENTORY);
        List<SysTag> list = sysTagService.selectSysTagList(sysTag);
        return getDataTable(list);
    }

    /**
     * 查詢庫存標籤下拉選項（不分頁）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.QUERY + "')")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) Integer status) {
        List<SysTag> list = sysTagService.selectInventoryTagList(status);
        return success(list);
    }

    /**
     * 取得標籤詳情
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.QUERY + "')")
    @GetMapping("/{tagId}")
    public AjaxResult getInfo(@PathVariable Long tagId) {
        return success(sysTagService.selectSysTagByTagId(tagId));
    }

    /**
     * 新增庫存標籤
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.ADD + "')")
    @Log(title = "庫存標籤", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTag sysTag) {
        if (!sysTagService.checkTagCodeUnique(sysTag)) {
            return error("新增標籤「" + sysTag.getTagName() + "」失敗，標籤代碼「" + sysTag.getTagCode() + "」已存在");
        }
        // 設定平台範圍為庫存
        sysTag.setPlatformScope(SysTag.SCOPE_INVENTORY);
        sysTag.setCreateBy(getUsername());

        int result = sysTagService.insertSysTag(sysTag);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("tagName", sysTag.getTagName());
        ajaxResult.put("tagCode", sysTag.getTagCode());
        log.info("新增庫存標籤：{}（{}）", sysTag.getTagName(), sysTag.getTagCode());
        return ajaxResult;
    }

    /**
     * 修改庫存標籤
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.EDIT + "')")
    @Log(title = "庫存標籤", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTag sysTag) {
        if (!sysTagService.checkTagCodeUnique(sysTag)) {
            return error("修改標籤「" + sysTag.getTagName() + "」失敗，標籤代碼「" + sysTag.getTagCode() + "」已存在");
        }
        sysTag.setUpdateBy(getUsername());

        int result = sysTagService.updateSysTag(sysTag);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("tagName", sysTag.getTagName());
        log.info("修改庫存標籤：{}", sysTag.getTagName());
        return ajaxResult;
    }

    /**
     * 刪除庫存標籤
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.REMOVE + "')")
    @Log(title = "庫存標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds) {
        // 先取得標籤名稱用於日誌
        StringBuilder tagNames = new StringBuilder();
        for (Long tagId : tagIds) {
            SysTag tag = sysTagService.selectSysTagByTagId(tagId);
            if (tag != null) {
                // 刪除物品標籤關聯
                invItemTagRelationService.unbindAllItems(tagId);
                if (!tagNames.isEmpty()) {
                    tagNames.append("、");
                }
                tagNames.append(tag.getTagName());
            }
        }

        int result = sysTagService.deleteSysTagByTagIds(tagIds);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("deletedTags", tagNames.toString());
        log.info("刪除庫存標籤：{}", tagNames);
        return ajaxResult;
    }

    // ==================== 物品貼標操作 ====================

    /**
     * 查詢物品標籤關聯列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BIND_ITEM + "')")
    @GetMapping("/relation/list")
    public TableDataInfo relationList(InvItemTagRelation relation) {
        startPage();
        List<InvItemTagRelation> list = invItemTagRelationService.selectInvItemTagRelationList(relation);
        return getDataTable(list);
    }

    /**
     * 查詢指定物品的標籤
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BIND_QUERY + "')")
    @GetMapping("/relation/item/{itemId}")
    public AjaxResult getItemTags(@PathVariable Long itemId) {
        List<InvItemTagRelation> list = invItemTagRelationService.selectByItemId(itemId);
        return success(list);
    }

    /**
     * 查詢指定標籤的物品
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BIND_QUERY + "')")
    @GetMapping("/relation/tag/{tagId}")
    public AjaxResult getTagItems(@PathVariable Long tagId) {
        List<InvItemTagRelation> list = invItemTagRelationService.selectByTagId(tagId);
        return success(list);
    }

    /**
     * 為物品貼標（單一物品，單一標籤）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BATCH_BIND + "')")
    @Log(title = "庫存物品貼標", businessType = BusinessType.INSERT)
    @PostMapping("/relation/bind")
    public AjaxResult bindTag(@RequestBody BindTagRequest request) {
        int result = invItemTagRelationService.bindTag(
                request.getItemId(),
                request.getTagId(),
                getUsername()
        );

        AjaxResult ajaxResult = result > 0 ? success("貼標成功") : success("標籤已存在");
        ajaxResult.put("itemId", request.getItemId());
        ajaxResult.put("tagId", request.getTagId());
        return ajaxResult;
    }

    /**
     * 批次為物品貼標
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BATCH_BIND + "')")
    @Log(title = "庫存批次貼標", businessType = BusinessType.INSERT)
    @PostMapping("/relation/batchBind")
    public AjaxResult batchBindTag(@RequestBody BatchBindTagRequest request) {
        int result = invItemTagRelationService.batchBindTag(
                request.getItemIds(),
                request.getTagId(),
                getUsername()
        );

        AjaxResult ajaxResult = success("成功貼標 " + result + " 個物品");
        ajaxResult.put("successCount", result);
        ajaxResult.put("tagId", request.getTagId());
        log.info("批次為 {} 個物品貼標，成功 {}", request.getItemIds().size(), result);
        return ajaxResult;
    }

    /**
     * 移除物品標籤
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.UNBIND + "')")
    @Log(title = "庫存移除標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/unbind")
    public AjaxResult unbindTag(@RequestBody BindTagRequest request) {
        int result = invItemTagRelationService.unbindTag(
                request.getItemId(),
                request.getTagId()
        );

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("itemId", request.getItemId());
        ajaxResult.put("tagId", request.getTagId());
        return ajaxResult;
    }

    /**
     * 批次刪除關聯
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.UNBIND + "')")
    @Log(title = "庫存批次移除標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/{ids}")
    public AjaxResult removeRelations(@PathVariable Long[] ids) {
        return toAjax(invItemTagRelationService.deleteByIds(ids));
    }

    /**
     * 批次貼標（含驗證和額外標籤支援）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BATCH_BIND + "')")
    @Log(title = "庫存批次貼標（驗證）", businessType = BusinessType.INSERT)
    @PostMapping("/relation/batchBindWithValidation")
    public AjaxResult batchBindWithValidation(@RequestBody BatchBindWithValidationRequest request) {
        Map<String, Object> result = invItemTagRelationService.batchBindWithValidation(
                request.getRecords(),
                request.getDefaultTagIds(),
                getUsername()
        );
        return success(result);
    }

    /**
     * 更新物品標籤（支援多標籤，可選擇替換現有標籤）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tag.Inventory.BATCH_BIND + "')")
    @Log(title = "庫存更新物品標籤", businessType = BusinessType.UPDATE)
    @PostMapping("/relation/updateTags")
    public AjaxResult updateItemTags(@RequestBody UpdateTagsRequest request) {
        Long itemId = request.getItemId();
        List<Long> tagIds = request.getTagIds();
        boolean replaceExisting = request.isReplaceExisting();

        // 如果需要替換，先移除現有標籤
        if (replaceExisting) {
            invItemTagRelationService.unbindAllTags(itemId);
        }

        // 貼上新標籤
        int result = 0;
        if (tagIds != null && !tagIds.isEmpty()) {
            result = invItemTagRelationService.batchBindTags(itemId, tagIds, getUsername());
        }

        AjaxResult ajaxResult = success("成功更新 " + result + " 個標籤");
        ajaxResult.put("successCount", result);
        ajaxResult.put("itemId", itemId);
        log.info("更新物品 {} 的標籤，共 {} 個", itemId, result);
        return ajaxResult;
    }

    // ==================== Request DTOs ====================

    /**
     * 貼標請求
     */
    @lombok.Data
    public static class BindTagRequest {
        private Long itemId;
        private Long tagId;
    }

    /**
     * 批次貼標請求
     */
    @lombok.Data
    public static class BatchBindTagRequest {
        private List<Long> itemIds;
        private Long tagId;
    }

    /**
     * 更新標籤請求（支援多標籤）
     */
    @lombok.Data
    public static class UpdateTagsRequest {
        private Long itemId;
        private List<Long> tagIds;
        private boolean replaceExisting;
    }

    /**
     * 批次貼標請求（含驗證和額外標籤）
     */
    @lombok.Data
    public static class BatchBindWithValidationRequest {
        private List<ItemTagRecord> records;
        private List<Long> defaultTagIds;
        private Integer batchNo;
        private Integer totalBatches;
    }

    /**
     * 物品貼標記錄
     */
    @lombok.Data
    public static class ItemTagRecord {
        private String itemCode;
        private String extraTagName;
    }
}
