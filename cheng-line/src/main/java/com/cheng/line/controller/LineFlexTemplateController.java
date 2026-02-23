package com.cheng.line.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.line.domain.LineFlexTemplate;
import com.cheng.line.service.ILineFlexTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE Flex 自訂範本 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/line/flex/template")
@RequiredArgsConstructor
public class LineFlexTemplateController extends BaseController {

    private final ILineFlexTemplateService lineFlexTemplateService;

    /**
     * 查詢 Flex 範本列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(LineFlexTemplate template) {
        startPage();
        List<LineFlexTemplate> list = lineFlexTemplateService.selectLineFlexTemplateList(template);
        return getDataTable(list);
    }

    /**
     * 查詢當前使用者可用的 Flex 範本（用於下拉選單）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.LIST + "')")
    @GetMapping("/available")
    public AjaxResult getAvailableTemplates() {
        List<LineFlexTemplate> list = lineFlexTemplateService.selectAvailableFlexTemplates();
        return success(list);
    }

    /**
     * 取得 Flex 範本詳情
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.QUERY + "')")
    @GetMapping("/{flexTemplateId}")
    public AjaxResult getInfo(@PathVariable Long flexTemplateId) {
        return success(lineFlexTemplateService.selectLineFlexTemplateById(flexTemplateId));
    }

    /**
     * 新增 Flex 範本
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.ADD + "')")
    @Log(title = "Flex範本管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody LineFlexTemplate template) {
        int result = lineFlexTemplateService.insertLineFlexTemplate(template);
        if (result > 0) {
            AjaxResult ajaxResult = success("儲存成功");
            ajaxResult.put("flexTemplateId", template.getFlexTemplateId());
            ajaxResult.put("templateName", template.getTemplateName());
            return ajaxResult;
        }
        return error("儲存失敗");
    }

    /**
     * 修改 Flex 範本
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.EDIT + "')")
    @Log(title = "Flex範本管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody LineFlexTemplate template) {
        return toAjax(lineFlexTemplateService.updateLineFlexTemplate(template));
    }

    /**
     * 刪除 Flex 範本
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Template.REMOVE + "')")
    @Log(title = "Flex範本管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{flexTemplateIds}")
    public AjaxResult remove(@PathVariable Long[] flexTemplateIds) {
        return toAjax(lineFlexTemplateService.deleteLineFlexTemplateByIds(flexTemplateIds));
    }
}
