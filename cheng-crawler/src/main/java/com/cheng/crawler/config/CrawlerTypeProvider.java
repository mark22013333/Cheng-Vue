package com.cheng.crawler.config;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.common.enums.TaskParamType;
import com.cheng.crawler.enums.CrawlerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爬蟲任務類型提供者
 * 向排程模組提供所有可用的爬蟲類型及其參數定義
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Slf4j
@Component
public class CrawlerTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.CRAWLER;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        return Arrays.stream(CrawlerType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 10; // 爬蟲任務優先級較高
    }

    /**
     * 建立任務類型選項
     * 顯示格式：[代號] 分類 - 功能名稱
     * 例如：[CA103] 臺灣證券交易所 - 上市公司每日收盤價
     * 
     * <p>參數定義已經在 CrawlerType enum 中實作，直接呼叫即可
     * <p>自動在參數列表前面加入 crawlerType 參數
     */
    private TaskTypeOption buildTaskTypeOption(CrawlerType type) {
        // 建立 crawlerType 參數（隱藏參數，固定值）
        TaskParamMetadata crawlerTypeParam = TaskParamMetadata.builder()
                .name("crawlerType")
                .label("爬蟲類型")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue(type.name()) // 預設值為當前類型代碼
                .description("爬蟲類型代碼（自動填入）")
                .order(0) // 第一個參數
                .visible(false) // 隱藏不顯示在前端
                .build();
        
        // 取得原始參數列表
        List<TaskParamMetadata> originalParams = type.buildParams();
        
        // 合併參數列表：crawlerType + 原始參數
        List<TaskParamMetadata> allParams = new ArrayList<>();
        allParams.add(crawlerTypeParam);
        if (originalParams != null) {
            allParams.addAll(originalParams);
        }
        
        return TaskTypeOption.builder()
                .code(type.name())
                .label(String.format("[%s] %s - %s",
                        type.name(),
                        type.getCategory(),
                        type.getFuncName()))
                .category(TaskCategory.CRAWLER.getCode())
                .description(String.format("從「%s」爬取「%s」資料",
                        type.getCategory(),
                        type.getFuncName()))
                .beanName("crawlerTask")
                .defaultMethod("run") // 使用統一的 run 方法（接收 JSON）
                .params(allParams) // 使用合併後的參數列表
                .enabled(true)
                .build();
    }

}
