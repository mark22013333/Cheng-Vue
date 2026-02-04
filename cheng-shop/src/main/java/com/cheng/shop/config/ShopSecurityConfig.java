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

    @Bean
    @Order(1)
    public SecurityFilterChain shopFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher("/shop/**")
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headersCustomizer) -> headersCustomizer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/shop/front/**").permitAll()
                        .requestMatchers("/shop/auth/**").permitAll()
                        .requestMatchers("/shop/payment/ecpay/callback", "/shop/payment/ecpay/return").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(memberAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(corsFilter, MemberAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }
}
