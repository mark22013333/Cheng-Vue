package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.ImageResizeUtil;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.framework.config.ServerConfig;
import com.cheng.line.domain.SysLineRichMenu;
import com.cheng.line.service.ISysLineRichMenuService;
import com.cheng.line.service.impl.SysLineRichMenuServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LINE Rich Menu (圖文選單) Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/line/richMenu")
public class SysLineRichMenuController extends BaseController {

    @Autowired
    private ISysLineRichMenuService richMenuService;
    
    @Autowired
    private ServerConfig serverConfig;

    /**
     * 查詢 Rich Menu 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLineRichMenu richMenu) {
        startPage();
        List<SysLineRichMenu> list = richMenuService.selectRichMenuList(richMenu);
        return getDataTable(list);
    }

    /**
     * 匯出 Rich Menu 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:export')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLineRichMenu richMenu) {
        List<SysLineRichMenu> list = richMenuService.selectRichMenuList(richMenu);
        ExcelUtil<SysLineRichMenu> util = new ExcelUtil<>(SysLineRichMenu.class);
        util.exportExcel(response, list, "Rich Menu 列表");
    }

    /**
     * 取得 Rich Menu 詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(richMenuService.selectRichMenuById(id));
    }

    /**
     * 根據 LINE richMenuId 查詢
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping("/byRichMenuId/{richMenuId}")
    public AjaxResult getByRichMenuId(@PathVariable("richMenuId") String richMenuId) {
        return success(richMenuService.selectRichMenuByRichMenuId(richMenuId));
    }

    /**
     * 根據頻道 ID 查詢預設選單
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping("/default/{configId}")
    public AjaxResult getDefaultByConfigId(@PathVariable("configId") Integer configId) {
        return success(richMenuService.selectDefaultRichMenuByConfigId(configId));
    }

    /**
     * 新增 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:add')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysLineRichMenu richMenu) {
        return toAjax(richMenuService.insertRichMenu(richMenu));
    }

    /**
     * 修改 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:edit')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysLineRichMenu richMenu) {
        return toAjax(richMenuService.updateRichMenu(richMenu));
    }

    /**
     * 刪除 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:remove')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(richMenuService.deleteRichMenuByIds(ids));
    }

    /**
     * 發布 Rich Menu 到 LINE 平台
     * 支援同步和異步模式（透過 taskId 參數控制）
     *
     * @param id     Rich Menu ID
     * @param taskId 任務 ID（可選，提供時使用異步 + SSE 模式）
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{id}")
    public AjaxResult publish(@PathVariable Long id, @RequestParam(required = false) String taskId) {
        // 如果有 taskId，使用異步模式
        if (StringUtils.isNotEmpty(taskId)) {
            // 呼叫異步方法，立即返回
            ((SysLineRichMenuServiceImpl) richMenuService).publishRichMenuAsync(id, taskId);
            return AjaxResult.success("發布任務已啟動，請等待進度通知");
        } else {
            // 同步模式（向下相容）
            String richMenuId = richMenuService.publishRichMenu(id);
            return AjaxResult.success("發布成功", richMenuId);
        }
    }

    /**
     * 設定為預設選單
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:setDefault')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/setDefault/{id}")
    public AjaxResult setDefault(@PathVariable Long id) {
        return toAjax(richMenuService.setDefaultRichMenu(id));
    }

    /**
     * 從 LINE 平台刪除 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteFromLine/{id}")
    public AjaxResult deleteFromLine(@PathVariable Long id) {
        return toAjax(richMenuService.deleteRichMenuFromLine(id));
    }

    /**
     * 綁定 Rich Menu 到使用者
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/linkToUser")
    public AjaxResult linkToUser(@RequestParam String userId, @RequestParam String richMenuId) {
        return toAjax(richMenuService.linkRichMenuToUser(userId, richMenuId));
    }

    /**
     * 解除使用者的 Rich Menu 綁定
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/unlinkFromUser/{userId}")
    public AjaxResult unlinkFromUser(@PathVariable String userId) {
        return toAjax(richMenuService.unlinkRichMenuFromUser(userId));
    }

    /**
     * 檢查選單名稱是否唯一
     */
    @GetMapping("/checkNameUnique")
    public AjaxResult checkNameUnique(SysLineRichMenu richMenu) {
        boolean unique = richMenuService.checkNameUnique(richMenu);
        return success(unique);
    }
    
    /**
     * Rich Menu 圖片上傳（支援自動調整尺寸）
     * 
     * @param file 上傳的圖片檔案
     * @param targetSize 目標尺寸（格式：寬x高，例如：2500x1686），可選
     * @return 上傳結果
     */
    @PostMapping("/uploadImage")
    public AjaxResult uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetSize", required = false) String targetSize) {
        
        try {
            // 驗證檔案
            if (file == null || file.isEmpty()) {
                return error("請選擇要上傳的圖片");
            }
            
            // 讀取原始圖片
            byte[] originalBytes = file.getBytes();
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (originalImage == null) {
                return error("無法讀取圖片，請確認檔案格式正確");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 結果資訊
            Map<String, Object> result = new HashMap<>();
            result.put("originalWidth", originalWidth);
            result.put("originalHeight", originalHeight);
            result.put("originalSize", originalWidth + "x" + originalHeight);
            
            byte[] finalBytes = originalBytes;
            boolean resized = false;
            
            // 如果指定了目標尺寸，檢查是否需要調整
            if (StringUtils.isNotEmpty(targetSize)) {
                String[] sizeParts = targetSize.split("x");
                if (sizeParts.length == 2) {
                    int targetWidth = Integer.parseInt(sizeParts[0]);
                    int targetHeight = Integer.parseInt(sizeParts[1]);
                    
                    // 如果尺寸不符，自動調整
                    if (originalWidth != targetWidth || originalHeight != targetHeight) {
                        log.info("圖片尺寸不符，自動調整：{}x{} -> {}x{}", 
                                originalWidth, originalHeight, targetWidth, targetHeight);
                        
                        // 使用 CROP 模式調整
                        finalBytes = ImageResizeUtil.resize(
                                originalBytes, 
                                targetWidth, 
                                targetHeight, 
                                ImageResizeUtil.ResizeMode.CROP
                        );
                        
                        // 檢查是否超過 1MB
                        long maxSize = 1024 * 1024;
                        if (ImageResizeUtil.exceedsSize(finalBytes, maxSize)) {
                            log.warn("調整後圖片超過 1MB，開始壓縮：{} bytes", finalBytes.length);
                            finalBytes = ImageResizeUtil.compressToSize(finalBytes, maxSize);
                        }
                        
                        resized = true;
                        result.put("resized", true);
                        result.put("finalWidth", targetWidth);
                        result.put("finalHeight", targetHeight);
                        result.put("finalSize", targetSize);
                    } else {
                        result.put("resized", false);
                        result.put("finalWidth", originalWidth);
                        result.put("finalHeight", originalHeight);
                        result.put("finalSize", originalWidth + "x" + originalHeight);
                    }
                }
            } else {
                result.put("resized", false);
                result.put("finalWidth", originalWidth);
                result.put("finalHeight", originalHeight);
                result.put("finalSize", originalWidth + "x" + originalHeight);
            }
            
            // 儲存檔案（使用 Rich Menu 專用路徑）
            String uploadPath = CoolAppsConfig.getRichMenuPath();
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 產生檔案名稱
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = IdUtils.fastSimpleUUID() + extension;
            String filePath = uploadPath + File.separator + fileName;
            
            // 寫入檔案
            Path path = Paths.get(filePath);
            Files.write(path, finalBytes);
            
            // 產生訪問 URL（注意：需要包含 /upload 路徑）
            String url = serverConfig.getUrl() + "/profile/upload/richmenu/" + fileName;
            
            // 返回結果
            result.put("url", url);
            result.put("fileName", "/profile/upload/richmenu/" + fileName);
            result.put("originalFilename", originalFilename);
            result.put("fileSize", finalBytes.length);
            result.put("fileSizeKB", finalBytes.length / 1024);
            
            log.info("圖片上傳成功：{} ({}x{}{}) -> {}", 
                    originalFilename, 
                    result.get("originalWidth"), 
                    result.get("originalHeight"),
                    resized ? " -> " + result.get("finalSize") : "",
                    url);
            
            return success(result);
            
        } catch (Exception e) {
            log.error("圖片上傳失敗：{}", e.getMessage(), e);
            return error("圖片上傳失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除已上傳的圖片檔案
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:add') or @ss.hasPermi('line:richMenu:edit')")
    @DeleteMapping("/deleteImage")
    public AjaxResult deleteImage(@RequestParam String fileName) {
        try {
            if (StringUtils.isBlank(fileName)) {
                return error("檔案名稱不能為空");
            }
            
            // 安全檢查：只允許刪除 richmenu 目錄下的檔案
            if (!fileName.startsWith("/profile/upload/richmenu/")) {
                return error("只能刪除 Rich Menu 圖片");
            }
            
            // 取得實際檔案路徑
            String actualFileName = fileName.replace("/profile/upload/richmenu/", "");
            String filePath = CoolAppsConfig.getRichMenuPath() + File.separator + actualFileName;
            
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    log.info("刪除圖片成功：{}", fileName);
                    return success("刪除成功");
                } else {
                    log.warn("刪除圖片失敗：{}", fileName);
                    return error("刪除失敗");
                }
            } else {
                log.warn("圖片不存在：{}", fileName);
                return success("檔案不存在或已刪除");
            }
        } catch (Exception e) {
            log.error("刪除圖片失敗：{}", e.getMessage(), e);
            return error("刪除失敗：" + e.getMessage());
        }
    }

    /**
     * 重新下載並更新預覽圖
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:edit')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/refreshPreviewImage/{id}")
    public AjaxResult refreshPreviewImage(@PathVariable Long id) {
        return toAjax(richMenuService.refreshPreviewImage(id));
    }
}
