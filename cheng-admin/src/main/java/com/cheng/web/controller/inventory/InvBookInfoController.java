package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.service.IInvBookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 書籍資訊 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/inventory/bookInfo")
public class InvBookInfoController extends BaseController {

    @Autowired
    private IInvBookInfoService invBookInfoService;

    /**
     * 查詢書籍資訊列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvBookInfo invBookInfo) {
        startPage();
        List<InvBookInfo> list = invBookInfoService.selectInvBookInfoList(invBookInfo);
        return getDataTable(list);
    }

    /**
     * 匯出書籍資訊列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:export')")
    @Log(title = "書籍資訊", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvBookInfo invBookInfo) {
        List<InvBookInfo> list = invBookInfoService.selectInvBookInfoList(invBookInfo);
        ExcelUtil<InvBookInfo> util = new ExcelUtil<>(InvBookInfo.class);
        util.exportExcel(response, list, "書籍資訊資料");
    }

    /**
     * 取得書籍資訊詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:query')")
    @GetMapping(value = "/{bookInfoId}")
    public AjaxResult getInfo(@PathVariable("bookInfoId") Long bookInfoId) {
        return success(invBookInfoService.selectInvBookInfoByBookInfoId(bookInfoId));
    }

    /**
     * 根據 ISBN 查詢書籍資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:query')")
    @GetMapping(value = "/isbn/{isbn}")
    public AjaxResult getInfoByIsbn(@PathVariable("isbn") String isbn) {
        return success(invBookInfoService.selectInvBookInfoByIsbn(isbn));
    }

    /**
     * 新增書籍資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:add')")
    @Log(title = "書籍資訊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InvBookInfo invBookInfo) {
        return toAjax(invBookInfoService.insertInvBookInfo(invBookInfo));
    }

    /**
     * 修改書籍資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:edit')")
    @Log(title = "書籍資訊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InvBookInfo invBookInfo) {
        return toAjax(invBookInfoService.updateInvBookInfo(invBookInfo));
    }

    /**
     * 刪除書籍資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:bookInfo:remove')")
    @Log(title = "書籍資訊", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bookInfoIds}")
    public AjaxResult remove(@PathVariable Long[] bookInfoIds) {
        return toAjax(invBookInfoService.deleteInvBookInfoByBookInfoIds(bookInfoIds));
    }
}
