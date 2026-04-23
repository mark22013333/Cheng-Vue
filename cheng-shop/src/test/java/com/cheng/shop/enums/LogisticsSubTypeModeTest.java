package com.cheng.shop.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LogisticsSubTypeMode} 單元測試。
 */
class LogisticsSubTypeModeTest {

    @Test
    @DisplayName("fromConfigValue: 解析大寫合法值")
    void fromConfigValue_validUpper_returnsMode() {
        assertThat(LogisticsSubTypeMode.fromConfigValue("B2C")).isEqualTo(LogisticsSubTypeMode.B2C);
        assertThat(LogisticsSubTypeMode.fromConfigValue("C2C")).isEqualTo(LogisticsSubTypeMode.C2C);
    }

    @ParameterizedTest(name = "fromConfigValue: 大小寫不敏感（{0}）")
    @ValueSource(strings = {"b2c", "B2c", "b2C", "B2C"})
    void fromConfigValue_caseInsensitive_returnsB2c(String raw) {
        assertThat(LogisticsSubTypeMode.fromConfigValue(raw)).isEqualTo(LogisticsSubTypeMode.B2C);
    }

    @ParameterizedTest(name = "fromConfigValue: 去除前後空白（\"{0}\"）")
    @ValueSource(strings = {"  B2C  ", "\tC2C\t", "\n B2C \n"})
    void fromConfigValue_trimsWhitespace(String raw) {
        LogisticsSubTypeMode mode = LogisticsSubTypeMode.fromConfigValue(raw);
        assertThat(mode).isIn(LogisticsSubTypeMode.B2C, LogisticsSubTypeMode.C2C);
    }

    @ParameterizedTest(name = "fromConfigValue: 非法值回預設 C2C（\"{0}\"）")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "ABC", "b2b", "B 2 C", "123"})
    void fromConfigValue_invalidOrEmpty_returnsDefaultC2c(String raw) {
        assertThat(LogisticsSubTypeMode.fromConfigValue(raw)).isEqualTo(LogisticsSubTypeMode.C2C);
    }

    @Test
    @DisplayName("DEFAULT 常數為 C2C")
    void defaultMode_isC2c() {
        assertThat(LogisticsSubTypeMode.DEFAULT).isEqualTo(LogisticsSubTypeMode.C2C);
    }
}
