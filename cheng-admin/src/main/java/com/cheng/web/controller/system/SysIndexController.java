package com.cheng.web.controller.system;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首頁
 *
 * @author cheng
 */
@RestController
public class SysIndexController {
    /**
     * 系統基礎配置
     */
    @Autowired
    private CoolAppsConfig coolAppsConfig;

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
}
