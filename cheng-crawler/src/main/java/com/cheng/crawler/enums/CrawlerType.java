package com.cheng.crawler.enums;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.enums.TaskParamType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 網路爬蟲類型的列舉
 * 此列舉定義了各種不同來源的網路爬蟲類型，每種類型對應特定的資料來源類別和功能名稱
 *
 * <p>每個爬蟲類型都定義了自己的參數配置，透過 buildParams() 方法返回
 *
 * @author Cheng
 * @since 2025-10-02 00:06
 **/
@Slf4j
@Getter
@RequiredArgsConstructor
public enum CrawlerType {

    /**
     * CA101 - ISBN 書籍查詢
     */
    CA101("ISBN書籍查詢", "書籍庫存") {
        @Override
        public List<TaskParamMetadata> buildParams() {
            return Collections.singletonList(
                    TaskParamMetadata.builder()
                            .name("isbn")
                            .label("ISBN")
                            .type(TaskParamType.STRING)
                            .required(true)
                            .description("書籍的 ISBN 編號")
                            .order(1)
                            .validation(TaskParamMetadata.ValidationRule.builder()
                                    .pattern("^(978|979)?[0-9]{9}[0-9X]$")
                                    .message("請輸入有效的 ISBN 編號（10 或 13 碼）")
                                    .build())
                            .build()
            );
        }
    },

    /**
     * CA102 - 臺灣證券交易所即時重大訊息
     */
    CA102("臺灣證券交易所", "即時重大訊息") {
        @Override
        public List<TaskParamMetadata> buildParams() {
            return Arrays.asList(
                    TaskParamMetadata.builder()
                            .name("mode")
                            .label("執行模式")
                            .type(TaskParamType.SELECT)
                            .required(true)
                            .defaultValue("today-only")
                            .description("選擇爬取資料的範圍")
                            .order(1)
                            .options(Arrays.asList(
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("today-only")
                                            .label("僅今日")
                                            .description("只爬取今天的重大訊息")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("full-sync")
                                            .label("完整同步")
                                            .description("從歷史資料開始完整爬取")
                                            .build()
                            ))
                            .build(),

                    TaskParamMetadata.builder()
                            .name("batchSize")
                            .label("批次大小")
                            .type(TaskParamType.NUMBER)
                            .required(false)
                            .defaultValue("500")
                            .description("每批次處理的資料筆數")
                            .order(2)
                            .validation(TaskParamMetadata.ValidationRule.builder()
                                    .min(100.0)
                                    .max(2000.0)
                                    .message("批次大小必須在 100-2000 之間")
                                    .build())
                            .build()
            );
        }
    },

    /**
     * CA103 - 臺灣證券交易所上市公司每日收盤價
     */
    CA103("臺灣證券交易所", "上市公司每日收盤價") {
        @Override
        public List<TaskParamMetadata> buildParams() {
            return Arrays.asList(
                    TaskParamMetadata.builder()
                            .name("mode")
                            .label("執行模式")
                            .type(TaskParamType.SELECT)
                            .required(true)
                            .defaultValue("today-only")
                            .description("選擇爬取資料的範圍")
                            .order(1)
                            .options(Arrays.asList(
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("today-only")
                                            .label("僅今日")
                                            .description("只爬取今天的收盤價資料")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("full-sync")
                                            .label("完整同步")
                                            .description("從 2016/08/01 開始完整爬取")
                                            .build(),
                                    TaskParamMetadata.OptionItem.builder()
                                            .value("date-range")
                                            .label("自訂日期範圍")
                                            .description("自訂開始和結束日期")
                                            .build()
                            ))
                            .build(),

                    TaskParamMetadata.builder()
                            .name("startDate")
                            .label("開始日期")
                            .type(TaskParamType.DATE)
                            .required(false)
                            .description("當模式為「自訂日期範圍」時使用")
                            .order(2)
                            .build(),

                    TaskParamMetadata.builder()
                            .name("endDate")
                            .label("結束日期")
                            .type(TaskParamType.DATE)
                            .required(false)
                            .description("當模式為「自訂日期範圍」時使用")
                            .order(3)
                            .build()
            );
        }
    };

    private final String category;
    private final String funcName;

    /**
     * 建立此爬蟲類型的參數列表
     * 每個爬蟲類型必須實作此方法，定義自己的參數
     *
     * @return 參數元數據列表
     */
    public abstract List<TaskParamMetadata> buildParams();

    /**
     * 根據代碼取得爬蟲類型
     *
     * @param code 代碼（例如：CA102、CA103）
     * @return 對應的爬蟲類型，若無則返回 null
     */
    public static CrawlerType of(String code) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
