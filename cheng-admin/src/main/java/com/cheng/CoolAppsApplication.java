package com.cheng;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * 啟動程式
 *
 * @author cheng
 */
@Slf4j
@EnableEncryptableProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CoolAppsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoolAppsApplication.class, args);
    }

    /**
     * 應用啟動完成後顯示存取網址
     */
    @Bean
    public ApplicationRunner applicationRunner(Environment env) {
        return args -> {
            String protocol = "http";
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "/");
            String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");

            String hostAddress = "localhost";
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                log.warn("無法取得本機 IP 位址，使用 localhost");
            }

            String localUrl = buildUrl(protocol, "localhost", serverPort, contextPath);
            String networkUrl = buildUrl(protocol, hostAddress, serverPort, contextPath);
            String swaggerUrl = buildUrl(protocol, "localhost", serverPort, contextPath, swaggerPath);
            String swaggerNetworkUrl = buildUrl(protocol, hostAddress, serverPort, contextPath, swaggerPath);
            log.info("""
                💻 Cool Apps — 部署成功的那一刻，是錯誤訊息的起點。 \s
                     ░▒▓██████▓▒░ ░▒▓██████▓▒░ ░▒▓██████▓▒░░▒▓█▓▒░              ░▒▓██████▓▒░░▒▓███████▓▒░░▒▓███████▓▒░ ░▒▓███████▓▒░\s
                    ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░       \s
                    ░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░       \s
                    ░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓████████▓▒░▒▓███████▓▒░░▒▓███████▓▒░ ░▒▓██████▓▒░ \s
                    ░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░             ░▒▓█▓▒░\s
                    ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░             ░▒▓█▓▒░\s
                     ░▒▓██████▓▒░ ░▒▓██████▓▒░ ░▒▓██████▓▒░░▒▓████████▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░      ░▒▓███████▓▒░ \s
                   \s""");
            log.info("""                        
                            ----------------------------------------------------------
                            \t\
                            🚀 應用程式執行中！存取網址：
                            \t\
                            本機存取: {}
                            \t\
                            網路存取: {}
                            \t\
                            Swagger API (本機): {}
                            \t\
                            Swagger API (網路): {}
                            \t\
                            環境設定檔: {}
                            ----------------------------------------------------------""",
                    localUrl, networkUrl, swaggerUrl, swaggerNetworkUrl,
                    env.getProperty("spring.profiles.active", "default"));
        };
    }

    /**
     * 建構 URL，確保路徑組合正確，不會出現雙斜線
     *
     * @param protocol 協定（http/https）
     * @param host     主機位址
     * @param port     埠號
     * @param paths    路徑片段（可變參數）
     * @return 完整的 URL
     */
    private static String buildUrl(String protocol, String host, String port, String... paths) {
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(host).append(":").append(port);

        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }

            // 移除路徑開頭的斜線（如果 URL 已經以斜線結尾）
            if (url.charAt(url.length() - 1) == '/' && path.startsWith("/")) {
                path = path.substring(1);
            }
            // 如果 URL 不以斜線結尾且路徑不以斜線開頭，加上斜線
            else if (url.charAt(url.length() - 1) != '/' && !path.startsWith("/")) {
                url.append('/');
            }

            url.append(path);
        }

        return url.toString();
    }
}
