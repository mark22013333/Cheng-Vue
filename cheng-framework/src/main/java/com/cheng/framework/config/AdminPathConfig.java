package com.cheng.framework.config;

import com.cheng.common.annotation.PublicApi;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 後台 API 統一前綴設定（已禁用）
 *
 * <p>原本對所有非 @PublicApi 的 Controller 加上 /cadm 前綴</p>
 * <p>為簡化架構，已禁用此配置，改由 nginx 統一處理路由</p>
 *
 * @author cheng
 */
// @Configuration  // 已禁用：簡化架構，後端 Controller 保持原始路徑
public class AdminPathConfig implements WebMvcConfigurer {

    private static final String ADMIN_PREFIX = "/cadm";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ADMIN_PREFIX, handlerType -> {
            boolean isController = AnnotatedElementUtils.hasAnnotation(handlerType, RestController.class)
                    || AnnotatedElementUtils.hasAnnotation(handlerType, Controller.class);
            boolean isPublic = AnnotatedElementUtils.hasAnnotation(handlerType, PublicApi.class);
            return isController && !isPublic;
        });
    }
}
