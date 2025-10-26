package com.cheng.web.core.config;

import com.cheng.common.config.CoolAppsConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 3 的設定
 *
 * @author cheng
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    /**
     * 系統基礎設定
     */
    private final CoolAppsConfig coolAppsConfig;

    /**
     * 自定義的 OpenAPI 物件
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI().components(new Components()
                        // 設定認證的請求標頭
                        .addSecuritySchemes("apikey", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("apikey"))
                .info(getApiInfo());
    }

    @Bean
    public SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("Authorization")
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer");
    }

    /**
     * 新增 API 摘要訊息
     */
    public Info getApiInfo() {
        return new Info()
                .title("CoolApps 管理系統 RESTful API")
                .description(
                        """
                                ## CoolApps 管理系統 API 文件
                                
                                提供完整的企業管理功能，包括：
                                - **系統管理**：使用者、角色、權限、選單管理
                                - **任務排程**：定時任務管理、爬蟲任務配置與執行
                                - **庫存管理**：庫存入出庫、QR Code 掃描、借出歸還管理
                                - **圖書管理**：圖書資訊、借閱管理、分類管理
                                - **系統監控**：線上使用者、登入日誌、操作日誌
                                
                                ### 認證方式
                                API 採用 JWT Token 認證，請在請求標頭中加入：
                                ```
                                Authorization: Bearer {token}
                                ```
                                
                                ### 技術架構
                                - **後端框架**：Spring Boot + MyBatis
                                - **安全框架**：Spring Security + JWT
                                - **資料庫**：MySQL + Druid 連線池
                                - **任務排程**：Quartz
                                - **API 文件**：Swagger 3 (OpenAPI)"""
                )
                .contact(new Contact()
                        .name(coolAppsConfig.getName())
                        .email("mark22013333@gmail.com"))
                .version(coolAppsConfig.getVersion());
    }
}
