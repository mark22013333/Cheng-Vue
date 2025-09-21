package com.cheng.framework.config;

import com.cheng.framework.config.properties.PermitAllUrlProperties;
import com.cheng.framework.security.filter.JwtAuthenticationTokenFilter;
import com.cheng.framework.security.handle.AuthenticationEntryPointImpl;
import com.cheng.framework.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * spring security配置
 *
 * @author cheng
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    /**
     * 認證失敗處理類
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * 登出處理類
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token認證過濾器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 跨域過濾器
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * 允許匿名訪問的地址
     */
    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    /**
     * 身份驗證實現
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * anyRequest          |   匹配所有請求路徑
     * access              |   SpringEl表達式結果為true時可以訪問
     * anonymous           |   匿名可以訪問
     * denyAll             |   使用者不能訪問
     * fullyAuthenticated  |   使用者完全認證可以訪問（非remember-me下自動登入）
     * hasAnyAuthority     |   如果有參數，參數表示權限，則其中任何一個權限可以訪問
     * hasAnyRole          |   如果有參數，參數表示角色，則其中任何一個角色可以訪問
     * hasAuthority        |   如果有參數，參數表示權限，則其權限可以訪問
     * hasIpAddress        |   如果有參數，參數表示IP位置，如果使用者IP和參數匹配，則可以訪問
     * hasRole             |   如果有參數，參數表示角色，則其角色可以訪問
     * permitAll           |   使用者可以任意訪問
     * rememberMe          |   允許通過remember-me登入的使用者訪問
     * authenticated       |   使用者登入後可訪問
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // CSRF禁用，因為不使用session
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用HTTP響應標頭
                .headers((headersCustomizer) -> headersCustomizer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // 認證失敗處理類
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // 基於token，所以不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 註解標記允許匿名訪問的url
                .authorizeHttpRequests((requests) -> {
                    permitAllUrl.getUrls().forEach(url -> requests.requestMatchers(url).permitAll());
                    // 對於登入login 註冊register 驗證碼captchaImage 允許匿名訪問
                    requests.requestMatchers("/login", "/register", "/captchaImage").permitAll()
                            // 靜態資源，可匿名訪問
                            .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/profile/**").permitAll()
                            .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/druid/**").permitAll()
                            // 除上面外的所有請求全部需要鑑權認證
                            .anyRequest().authenticated();
                })
                // 新增Logout filter
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                // 新增JWT filter
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 新增CORS filter
                .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }

    /**
     * 强散列哈希加密實現
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
