package com.cheng;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 啟動程序
 *
 * @author cheng
 */
@EnableEncryptableProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CoolAppsApplication {
    private static final Logger log = LoggerFactory.getLogger(CoolAppsApplication.class);
    
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(CoolAppsApplication.class, args);
        log.info("""
                ─=≡Σ((( つ•̀ω•́)つ  CoolApps 啟動成功   ( • ̀ω•́  )✧ﾞ \s
                 ,-----.             ,--.  ,---.
                '  .--./ ,---. ,---. |  | /  O  \\  ,---.  ,---.  ,---.
                |  |    | .-. | .-. ||  ||  .-.  || .-. || .-. |(  .-'
                '  '--'\\' '-' ' '-' '|  ||  | |  || '-' '| '-' '.-'  `)
                 `-----' `---' `---' `--'`--' `--'|  |-' |  |-' `----'
                                                  `--'   `--'""");
    }
}
