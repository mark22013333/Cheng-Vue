package com.cheng.shop.config;

import com.cheng.framework.security.handle.AuthenticationEntryPointImpl;
import com.cheng.shop.security.MemberAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * 商城會員安全鏈設定
 *
 * @author cheng
 */
@Configuration
@RequiredArgsConstructor
public class ShopSecurityConfig {

    private final AuthenticationEntryPointImpl unauthorizedHandler;
    private final MemberAuthenticationTokenFilter memberAuthenticationTokenFilter;
    private final CorsFilter corsFilter;

    /**
     * 商城前台安全鏈 - 僅處理會員端請求
     * 使用 Member-Token 進行認證
     *
     * 路徑規則：
     * - /shop/front/** : 商城前台公開 API（商品列表、文章等）
     * - /shop/auth/** : 會員認證（登入、註冊）
     * - /shop/my/** : 會員個人資料（訂單、地址等）- 需要 Member-Token
     *
     * 注意：其他 /shop/** API（如 /shop/banner/list、/shop/member/list）
     * 屬於後台管理，由主 SecurityConfig 處理，使用 Admin Token
     */
    @Bean
    @Order(1)
    public SecurityFilterChain shopFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // 只匹配商城前台會員端路徑
                .securityMatcher(
                        "/shop/front/**",
                        "/shop/auth/**",
                        "/shop/my/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headersCustomizer) -> headersCustomizer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/shop/front/**").permitAll()
                        .requestMatchers("/shop/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(memberAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(corsFilter, MemberAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }
}
