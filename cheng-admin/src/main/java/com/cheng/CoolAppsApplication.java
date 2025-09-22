package com.cheng;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 啟動程序
 *
 * @author cheng
 */
@Slf4j
@EnableEncryptableProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CoolAppsApplication {
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
