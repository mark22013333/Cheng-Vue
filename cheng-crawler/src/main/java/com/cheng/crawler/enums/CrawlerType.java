package com.cheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 網路爬蟲類型的列舉
 * 此列舉定義了各種不同來源的網路爬蟲類型，每種類型對應特定的資料來源類別和功能名稱
 *
 * @author Cheng
 * @since 2025-10-02 00:06
 **/
@Slf4j
@Getter
@AllArgsConstructor
public enum CrawlerType {

    CA101("全國新書資訊網", ""),

    ;

    private final String category;
    private final String funcName;

    public static CrawlerType of(String code) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }
}
