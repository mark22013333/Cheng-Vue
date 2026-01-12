package com.cheng.web.controller.tag;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.line.domain.LineUserTagRelation;
import com.cheng.line.service.ILineUserTagRelationService;
import com.cheng.line.service.impl.LineUserTagRelationServiceImpl;
import com.cheng.system.domain.SysTag;
import com.cheng.system.service.ISysTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LINE 標籤管理 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/tag/line")
@RequiredArgsConstructor
public class LineTagController extends BaseController {

    private final ISysTagService sysTagService;
    private final ILineUserTagRelationService lineUserTagRelationService;

    // ==================== 標籤定義 CRUD ====================

    /**
     * 查詢 LINE 標籤列表
     */
    @PreAuthorize("@ss.hasPermi('tag:line:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTag sysTag) {
        startPage();
        // 設定平台範圍為 LINE
        sysTag.setPlatformScope(SysTag.SCOPE_LINE);
        List<SysTag> list = sysTagService.selectSysTagList(sysTag);
        // 填充每個標籤的使用者數量
        for (SysTag tag : list) {
            int userCount = lineUserTagRelationService.countUsersByTagId(tag.getTagId());
            tag.setUserCount(userCount);
        }
        return getDataTable(list);
    }

    /**
     * 查詢 LINE 標籤下拉選項（不分頁）
     */
    @PreAuthorize("@ss.hasPermi('tag:line:query')")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) Integer status) {
        List<SysTag> list = sysTagService.selectLineTagList(status);
        return success(list);
    }

    /**
     * 取得標籤詳情
     */
    @PreAuthorize("@ss.hasPermi('tag:line:query')")
    @GetMapping("/{tagId}")
    public AjaxResult getInfo(@PathVariable Long tagId) {
        return success(sysTagService.selectSysTagByTagId(tagId));
    }

    /**
     * 新增 LINE 標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:add')")
    @Log(title = "LINE 標籤", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTag sysTag) {
        if (!sysTagService.checkTagCodeUnique(sysTag)) {
            return error("新增標籤「" + sysTag.getTagName() + "」失敗，標籤代碼「" + sysTag.getTagCode() + "」已存在");
        }
        // 設定平台範圍為 LINE
        sysTag.setPlatformScope(SysTag.SCOPE_LINE);
        sysTag.setCreateBy(getUsername());

        int result = sysTagService.insertSysTag(sysTag);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("tagName", sysTag.getTagName());
        ajaxResult.put("tagCode", sysTag.getTagCode());
        log.info("新增 LINE 標籤：{}（{}）", sysTag.getTagName(), sysTag.getTagCode());
        return ajaxResult;
    }

    /**
     * 修改 LINE 標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:edit')")
    @Log(title = "LINE 標籤", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTag sysTag) {
        if (!sysTagService.checkTagCodeUnique(sysTag)) {
            return error("修改標籤「" + sysTag.getTagName() + "」失敗，標籤代碼「" + sysTag.getTagCode() + "」已存在");
        }
        sysTag.setUpdateBy(getUsername());

        int result = sysTagService.updateSysTag(sysTag);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("tagName", sysTag.getTagName());
        log.info("修改 LINE 標籤：{}", sysTag.getTagName());
        return ajaxResult;
    }

    /**
     * 刪除 LINE 標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:remove')")
    @Log(title = "LINE 標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds) {
        // 先取得標籤名稱用於日誌
        StringBuilder tagNames = new StringBuilder();
        for (Long tagId : tagIds) {
            SysTag tag = sysTagService.selectSysTagByTagId(tagId);
            if (tag != null) {
                // 刪除 LINE 使用者標籤關聯
                lineUserTagRelationService.unbindAllUsers(tagId);
                if (!tagNames.isEmpty()) {
                    tagNames.append("、");
                }
                tagNames.append(tag.getTagName());
            }
        }

        int result = sysTagService.deleteSysTagByTagIds(tagIds);

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("deletedTags", tagNames.toString());
        log.info("刪除 LINE 標籤：{}", tagNames);
        return ajaxResult;
    }

    // ==================== 使用者貼標操作 ====================

    /**
     * 查詢使用者標籤關聯列表
     */
    @PreAuthorize("@ss.hasPermi('tag:line:bindUser')")
    @GetMapping("/relation/list")
    public TableDataInfo relationList(LineUserTagRelation relation) {
        startPage();
        List<LineUserTagRelation> list = lineUserTagRelationService.selectLineUserTagRelationList(relation);
        return getDataTable(list);
    }

    /**
     * 查詢指定使用者的標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:bindQuery')")
    @GetMapping("/relation/user/{lineUserId}")
    public AjaxResult getUserTags(@PathVariable String lineUserId) {
        List<LineUserTagRelation> list = lineUserTagRelationService.selectByLineUserId(lineUserId);
        return success(list);
    }

    /**
     * 查詢指定標籤的使用者
     */
    @PreAuthorize("@ss.hasPermi('tag:line:bindQuery')")
    @GetMapping("/relation/tag/{tagId}")
    public AjaxResult getTagUsers(@PathVariable Long tagId) {
        List<LineUserTagRelation> list = lineUserTagRelationService.selectByTagId(tagId);
        return success(list);
    }

    /**
     * 為使用者貼標（單一使用者，單一標籤）
     */
    @PreAuthorize("@ss.hasPermi('tag:line:batchBind')")
    @Log(title = "LINE 使用者貼標", businessType = BusinessType.INSERT)
    @PostMapping("/relation/bind")
    public AjaxResult bindTag(@RequestBody BindTagRequest request) {
        int result = lineUserTagRelationService.bindTag(
                request.getLineUserId(),
                request.getTagId(),
                getUsername()
        );

        AjaxResult ajaxResult = result > 0 ? success("貼標成功") : success("標籤已存在");
        ajaxResult.put("lineUserId", request.getLineUserId());
        ajaxResult.put("tagId", request.getTagId());
        return ajaxResult;
    }

    /**
     * 批次為使用者貼標（支援多標籤）
     */
    @PreAuthorize("@ss.hasPermi('tag:line:batchBind')")
    @Log(title = "LINE 批次貼標", businessType = BusinessType.INSERT)
    @PostMapping("/relation/batchBind")
    public AjaxResult batchBindTag(@RequestBody BatchBindTagRequest request) {
        int result;
        // 支援多標籤批次貼標
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            result = lineUserTagRelationService.batchBindTags(
                    request.getLineUserIds(),
                    request.getTagIds(),
                    getUsername()
            );
            log.info("批次為 {} 個使用者貼 {} 個標籤，成功 {}",
                    request.getLineUserIds().size(), request.getTagIds().size(), result);
        } else {
            // 向後相容：單一標籤
            result = lineUserTagRelationService.batchBindTag(
                    request.getLineUserIds(),
                    request.getTagId(),
                    getUsername()
            );
            log.info("批次為 {} 個使用者貼標，成功 {}", request.getLineUserIds().size(), result);
        }

        AjaxResult ajaxResult = success("成功貼標 " + result + " 筆");
        ajaxResult.put("successCount", result);
        return ajaxResult;
    }

    /**
     * 移除使用者標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:unbind')")
    @Log(title = "LINE 移除標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/unbind")
    public AjaxResult unbindTag(@RequestBody BindTagRequest request) {
        int result = lineUserTagRelationService.unbindTag(
                request.getLineUserId(),
                request.getTagId()
        );

        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("lineUserId", request.getLineUserId());
        ajaxResult.put("tagId", request.getTagId());
        return ajaxResult;
    }

    /**
     * 批次刪除關聯
     */
    @PreAuthorize("@ss.hasPermi('tag:line:unbind')")
    @Log(title = "LINE 批次移除標籤", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/{ids}")
    public AjaxResult removeRelations(@PathVariable Long[] ids) {
        return toAjax(lineUserTagRelationService.deleteByIds(ids));
    }

    /**
     * 更新使用者標籤（支援多標籤，可選擇替換現有標籤）
     */
    @PreAuthorize("@ss.hasPermi('tag:line:batchBind')")
    @Log(title = "LINE 更新使用者標籤", businessType = BusinessType.UPDATE)
    @PostMapping("/relation/updateTags")
    public AjaxResult updateUserTags(@RequestBody UpdateTagsRequest request) {
        String lineUserId = request.getLineUserId();
        List<Long> tagIds = request.getTagIds();
        boolean replaceExisting = request.isReplaceExisting();

        // 如果需要替換，先移除現有標籤
        if (replaceExisting) {
            lineUserTagRelationService.unbindAllTags(lineUserId);
        }

        // 貼上新標籤
        int result = 0;
        if (tagIds != null && !tagIds.isEmpty()) {
            result = lineUserTagRelationService.batchBindTags(lineUserId, tagIds, getUsername());
        }

        AjaxResult ajaxResult = success("成功更新 " + result + " 個標籤");
        ajaxResult.put("successCount", result);
        ajaxResult.put("lineUserId", lineUserId);
        log.info("更新使用者 {} 的標籤，共 {} 個", lineUserId, result);
        return ajaxResult;
    }

    /**
     * 批次為使用者貼標（含驗證和額外標籤支援）
     * 支援大量資料分批處理，會驗證使用者是否存在，並自動建立不存在的標籤
     */
    @PreAuthorize("@ss.hasPermi('tag:line:batchBind')")
    @Log(title = "LINE 批次貼標（含驗證）", businessType = BusinessType.INSERT)
    @PostMapping("/relation/batchBindWithValidation")
    public AjaxResult batchBindWithValidation(@RequestBody BatchBindWithValidationRequest request) {
        List<TagRecord> records = request.getRecords();
        List<Long> defaultTagIds = request.getDefaultTagIds();
        String createBy = getUsername();

        if (records == null || records.isEmpty()) {
            return error("記錄列表不能為空");
        }
        if (defaultTagIds == null || defaultTagIds.isEmpty()) {
            return error("預設標籤不能為空");
        }

        log.info("開始處理批次貼標，批次 {}/{}，記錄數：{}",
                request.getBatchNo(), request.getTotalBatches(), records.size());

        // 調用 Service 處理批次貼標
        Map<String, Object> result = lineUserTagRelationService.batchBindWithValidation(
                records.stream()
                        .map(r -> new LineUserTagRelationServiceImpl.TagRecordDTO(r.getLineUserId(), r.getExtraTagName()))
                        .toList(),
                defaultTagIds,
                createBy
        );

        AjaxResult ajaxResult = success("批次處理完成");
        ajaxResult.put("successCount", result.get("successCount"));
        ajaxResult.put("failedCount", result.get("failedCount"));
        ajaxResult.put("failedRecords", result.get("failedRecords"));
        ajaxResult.put("newTagsCreated", result.get("newTagsCreated"));
        ajaxResult.put("batchNo", request.getBatchNo());

        log.info("批次 {} 處理完成，成功：{}，失敗：{}",
                request.getBatchNo(), result.get("successCount"), result.get("failedCount"));

        return ajaxResult;
    }

    // ==================== Request DTOs ====================

    /**
     * 貼標請求
     */
    @lombok.Data
    public static class BindTagRequest {
        private String lineUserId;
        private Long tagId;
    }

    /**
     * 批次貼標請求（支援多標籤）
     */
    @lombok.Data
    public static class BatchBindTagRequest {
        private List<String> lineUserIds;
        private Long tagId;
        private List<Long> tagIds;
    }

    /**
     * 更新標籤請求（支援多標籤）
     */
    @lombok.Data
    public static class UpdateTagsRequest {
        private String lineUserId;
        private List<Long> tagIds;
        private boolean replaceExisting;
    }

    /**
     * 批次貼標請求（含驗證和額外標籤）
     */
    @lombok.Data
    public static class BatchBindWithValidationRequest {
        private List<TagRecord> records;
        private List<Long> defaultTagIds;
        private Integer batchNo;
        private Integer totalBatches;
    }

    /**
     * 貼標記錄
     */
    @lombok.Data
    public static class TagRecord {
        private String lineUserId;
        private String extraTagName;
    }
}
