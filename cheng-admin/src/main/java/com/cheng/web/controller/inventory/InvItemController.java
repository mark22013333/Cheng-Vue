package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvItem;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IInvItemService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 物品資訊Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/item")
@RequiredArgsConstructor
public class InvItemController extends BaseController {

    private final IInvItemService invItemService;
    private final InvItemMapper invItemMapper;

    /**
     * 查詢物品資訊列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvItem invItem) {
        startPage();
        List<InvItem> list = invItemService.selectInvItemList(invItem);
        return getDataTable(list);
    }

    /**
     * 查詢低庫存物品列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:list')")
    @GetMapping("/lowStock")
    public TableDataInfo lowStockList() {
        startPage();
        List<InvItem> list = invItemService.selectLowStockItemList();
        return getDataTable(list);
    }

    /**
     * 匯出物品資訊列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:export')")
    @Log(title = "物品資訊", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvItem invItem) {
        List<InvItem> list = invItemService.selectInvItemList(invItem);
        ExcelUtil<InvItem> util = new ExcelUtil<InvItem>(InvItem.class);
        util.exportExcel(response, list, "物品資訊資料");
    }

    /**
     * 取得物品資訊詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId) {
        return success(invItemService.selectInvItemByItemId(itemId));
    }

    /**
     * 根據物品編碼取得物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:query')")
    @GetMapping(value = "/code/{itemCode}")
    public AjaxResult getInfoByCode(@PathVariable("itemCode") String itemCode) {
        InvItem item = invItemService.selectInvItemByItemCode(itemCode);
        if (item != null) {
            return success(item);
        } else {
            return error("未找到對應的物品資訊");
        }
    }

    /**
     * 掃描條碼或QR碼取得物品資訊（包含庫存信息）
     * 權限：需要掃描功能權限（手機端使用）
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
    @PostMapping("/scan")
    public AjaxResult scanItem(@RequestBody ScanRequest scanRequest) {
        try {
            // 1. 先透過條碼或QR碼查找物品
            InvItem item = invItemService.scanItemByCode(scanRequest.getScanCode(), scanRequest.getScanType());

            if (item == null) {
                return error("未找到對應的物品");
            }

            // 2. 查詢包含庫存信息的完整資料
            InvItemWithStockDTO itemWithStock = invItemMapper.selectItemWithStockByItemId(item.getItemId());

            // 3. 計算庫存狀態和價值
            if (itemWithStock != null) {
                itemWithStock.calculateStockStatus();
                itemWithStock.calculateStockValue();
                return success(itemWithStock);
            } else {
                // 如果沒有庫存記錄，返回物品基本信息
                return success(item);
            }
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 新增物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:add')")
    @Log(title = "物品資訊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvItem invItem) {
        if (!invItemService.checkItemCodeUnique(invItem)) {
            return error("新增物品'" + invItem.getItemName() + "'失敗，物品編碼已存在");
        }
        if (!invItemService.checkBarcodeUnique(invItem)) {
            return error("新增物品'" + invItem.getItemName() + "'失敗，條碼已存在");
        }
        return toAjax(invItemService.insertInvItem(invItem));
    }

    /**
     * 修改物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:edit')")
    @Log(title = "物品資訊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InvItem invItem) {
        if (!invItemService.checkItemCodeUnique(invItem)) {
            return error("修改物品'" + invItem.getItemName() + "'失敗，物品編碼已存在");
        }
        if (!invItemService.checkBarcodeUnique(invItem)) {
            return error("修改物品'" + invItem.getItemName() + "'失敗，條碼已存在");
        }
        return toAjax(invItemService.updateInvItem(invItem));
    }

    /**
     * 刪除物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:remove')")
    @Log(title = "物品資訊", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(invItemService.deleteInvItemByItemIds(itemIds));
    }

    /**
     * 匯入物品資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:item:import')")
    @Log(title = "物品資訊", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<InvItem> util = new ExcelUtil<InvItem>(InvItem.class);
        List<InvItem> itemList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = invItemService.importItem(itemList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下載匯入範本
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<InvItem> util = new ExcelUtil<InvItem>(InvItem.class);
        util.importTemplateExcel(response, "物品資料");
    }

    /**
     * 掃描請求物件
     */
    @Setter
    @Getter
    public static class ScanRequest {
        private String scanCode;
        private String scanType; // 1條碼 2QR碼
    }
}
