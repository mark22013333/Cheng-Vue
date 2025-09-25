package com.cheng.framework.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static com.google.code.kaptcha.Constants.*;

/**
 * 驗證碼設定（優化樣式）
 * <p>
 * 更柔和的配色與更清晰的可讀性：
 * - 去除邊框
 * - 使用輕柔漸層背景
 * - 文字採深灰色，輕水波紋效果
 * - 減少噪點，提升辨識度
 *
 * @author cheng
 */
@Configuration
public class CaptchaConfig {
    @Bean(name = "captchaProducer")
    public DefaultKaptcha getKaptchaBean() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有邊框（改為無邊框，視覺更簡潔）
        properties.setProperty(KAPTCHA_BORDER, "no");
        // 驗證碼文字顏色（深灰）
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "34,34,34");
        // 驗證碼圖片寬高
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 文字大小與間距
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "40");
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "4");
        // Session Key
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCode");
        // 文字長度
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        // 字型（包含中英文常見字型，缺字時會回退）
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier,Microsoft YaHei,宋體,楷體");
        // 背景漸層（淺灰藍到更淺的藍白）
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_FROM, "247,249,252");
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_TO, "220,235,255");
        // 干擾與樣式（少噪點+柔和水紋）
        properties.setProperty(KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.WaterRipple");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    @Bean(name = "captchaProducerMath")
    public DefaultKaptcha getKaptchaBeanMath() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有邊框（改為無邊框）
        properties.setProperty(KAPTCHA_BORDER, "no");
        // 文字顏色（深灰）
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "40,40,40");
        // 圖片尺寸
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 字體大小與間距
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "40");
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "4");
        // Session Key
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCodeMath");
        // 自訂文字產生器（保留原有邏輯）
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL, "com.cheng.framework.config.KaptchaTextCreator");
        // 文字長度（數學算式通常較長，維持 6）
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "6");
        // 字型
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier,Microsoft YaHei,宋體,楷體");
        // 背景色（白到愛麗絲藍，清爽）
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_FROM, "255,255,255");
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_TO, "240,248,255");
        // 少噪點與柔和水紋
        properties.setProperty(KAPTCHA_NOISE_COLOR, "white");
        properties.setProperty(KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.WaterRipple");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
