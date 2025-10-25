package com.cheng.quartz.enums;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.enums.TaskParamType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 推播訊息類型列舉
 * 定義各種推播訊息類型及其參數配置
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    /**
     * LINE 推播訊息
     */
    LINE_PUSH("LINE Bot", "訊息推播") {
        @Override
        public List<TaskParamMetadata> buildParams() {
            return Arrays.asList(
                    TaskParamMetadata.builder()
                            .name("messageType")
                            .label("訊息類型")
                            .type(TaskParamType.SELECT)
                            .required(true)
                            .defaultValue("text")
                            .description("選擇推播的訊息類型")
                            .order(1)
                            .options(Arrays.asList(
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("text")
                                            .label("純文字訊息")
                                            .description("發送純文字訊息")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("flex")
                                            .label("Flex Message")
                                            .description("發送 Flex Message 樣板")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("image")
                                            .label("圖片訊息")
                                            .description("發送圖片")
                                            .build()
                            ))
                            .build(),

                    TaskParamMetadata.builder()
                            .name("targetType")
                            .label("推播對象")
                            .type(TaskParamType.SELECT)
                            .required(true)
                            .defaultValue("user")
                            .description("選擇推播的對象類型")
                            .order(2)
                            .options(Arrays.asList(
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("user")
                                            .label("個別使用者")
                                            .description("推播給特定使用者")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("group")
                                            .label("群組")
                                            .description("推播給群組")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("broadcast")
                                            .label("廣播")
                                            .description("推播給所有好友")
                                            .build()
                            ))
                            .build(),

                    TaskParamMetadata.builder()
                            .name("targetId")
                            .label("對象 ID")
                            .type(TaskParamType.STRING)
                            .required(false)
                            .description("使用者 ID 或群組 ID（廣播模式不需要）")
                            .order(3)
                            .build(),

                    TaskParamMetadata.builder()
                            .name("message")
                            .label("訊息內容")
                            .type(TaskParamType.TEXTAREA)
                            .required(true)
                            .description("要推播的訊息內容或 JSON 格式的 Flex Message")
                            .order(4)
                            .validation(TaskParamMetadata.ValidationRule.builder()
                                    .maxLength(5000)
                                    .message("訊息內容不得超過 5000 字元")
                                    .build())
                            .build()
            );
        }
    };

    private final String category;
    private final String funcName;

    /**
     * 建立此推播類型的參數列表
     * 每個推播類型必須實作此方法，定義自己的參數
     *
     * @return 參數元數據列表
     */
    public abstract List<TaskParamMetadata> buildParams();

    /**
     * 根據代碼取得推播類型
     *
     * @param code 代碼（例如：LINE_PUSH）
     * @return 對應的推播類型，若無則返回 null
     */
    public static NotificationType of(String code) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
