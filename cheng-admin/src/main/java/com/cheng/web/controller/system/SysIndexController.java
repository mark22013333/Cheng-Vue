package com.cheng.web.controller.system;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首頁
 *
 * @author cheng
 */
@RestController
public class SysIndexController {
    /**
     * 系統基礎設定
     */
    @Autowired
    private CoolAppsConfig coolAppsConfig;

    /**
     * 文件上傳配置
     */
    @Autowired
    private MultipartProperties multipartProperties;

    /**
     * 訪問首頁，提示語
     */
    @RequestMapping("/")
    public String index() {
        return StringUtils.format("""
                ✨ 歡迎搭乘 {} 後台管理框架 ✨
                \uD83D\uDE80 目前版本：v{}
                \uD83D\uDC49 請從前端入口進入，開啟你的專屬操作之旅吧！""", coolAppsConfig.getName(), coolAppsConfig.getVersion());
    }

    /**
     * 取得系統配置資訊（供前端使用）
     */
    @GetMapping("/system/config/info")
    public AjaxResult getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 取得文件上傳配置
        DataSize maxFileSize = multipartProperties.getMaxFileSize();
        DataSize maxRequestSize = multipartProperties.getMaxRequestSize();
        
        Map<String, Object> uploadConfig = new HashMap<>();
        uploadConfig.put("maxFileSize", maxFileSize.toBytes());  // 單一檔案大小（bytes）
        uploadConfig.put("maxFileSizeMB", maxFileSize.toMegabytes());  // 單一檔案大小（MB）
        uploadConfig.put("maxRequestSize", maxRequestSize.toBytes());  // 總請求大小（bytes）
        uploadConfig.put("maxRequestSizeMB", maxRequestSize.toMegabytes());  // 總請求大小（MB）
        
        config.put("upload", uploadConfig);
        config.put("systemName", coolAppsConfig.getName());
        config.put("version", coolAppsConfig.getVersion());
        
        return AjaxResult.success(config);
    }
}
