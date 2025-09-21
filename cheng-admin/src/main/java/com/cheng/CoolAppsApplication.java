package com.cheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 啟動程序
 *
 * @author cheng
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CoolAppsApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(CoolAppsApplication.class, args);
        System.out.println("""
                ─=≡Σ((( つ•̀ω•́)つ  CoolApps 啟動成功   ( • ̀ω•́  )✧ﾞ \s
                 ,-----.             ,--.  ,---.
                '  .--./ ,---. ,---. |  | /  O  \\  ,---.  ,---.  ,---.
                |  |    | .-. | .-. ||  ||  .-.  || .-. || .-. |(  .-'
                '  '--'\\' '-' ' '-' '|  ||  | |  || '-' '| '-' '.-'  `)
                 `-----' `---' `---' `--'`--' `--'|  |-' |  |-' `----'
                                                  `--'   `--'""");
    }
}
