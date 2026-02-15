package com.cheng.shop.controller;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.utils.file.FileUploadUtils;
import com.cheng.common.utils.file.FileUtils;
import com.cheng.framework.config.ServerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商城會員檔案上傳
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/shop/member/upload")
@RequiredArgsConstructor
public class ShopMemberUploadController {

    private final ServerConfig serverConfig;

    /**
     * 會員圖片上傳
     *
     * @param file     上傳檔案
     * @param category 分類名稱（member/avatar 等）
     */
    @PostMapping
    public AjaxResult upload(MultipartFile file, String category) {
        try {
            // 上傳檔案路徑（商城專用目錄）
            String filePath = CoolAppsConfig.getShopUploadPath(category);
            // 上傳並返回新檔案名稱
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        } catch (Exception e) {
            log.error("會員上傳檔案失敗", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}
