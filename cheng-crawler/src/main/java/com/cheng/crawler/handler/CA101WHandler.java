package com.cheng.crawler.handler;

import com.cheng.crawler.CrawlerHandler;
import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.enums.CrawlerType;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Cheng
 * @since 2025-10-02 00:21
 **/
@Slf4j
@Component
public class CA101WHandler extends CrawlerHandler {

    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA101;
    }

    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) throws Exception {
        return List.of();
    }

}
