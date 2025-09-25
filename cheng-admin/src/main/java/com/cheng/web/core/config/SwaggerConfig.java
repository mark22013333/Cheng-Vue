package com.cheng.web.core.config;

import com.cheng.common.config.CoolAppsConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 3 的介面設定
 *
 * @author cheng
 */
@Configuration
public class SwaggerConfig {
    /**
     * 系統基礎設定
     */
    @Autowired
    private CoolAppsConfig coolAppsConfig;

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
     * 新增摘要訊息
     */
    public Info getApiInfo() {
        return new Info()
                .title("標題：CoolApps管理系統-介面檔案")
                .description("描述：測試用...")
                .contact(new Contact().name(coolAppsConfig.getName()))
                .version("版本:" + coolAppsConfig.getVersion());
    }
}
