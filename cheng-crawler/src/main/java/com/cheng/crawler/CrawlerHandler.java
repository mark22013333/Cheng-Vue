package com.cheng.crawler;

import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.enums.CrawlerType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cheng
 * @since 2025-10-02 00:06
 **/
@Slf4j
@Component
public abstract class CrawlerHandler implements ApplicationContextAware {

    private static final Map<CrawlerType, CrawlerHandler> HANDLER_MAP = new HashMap<>();

    private static ApplicationContext applicationContext;

    @PostConstruct
    public void handlerInit() {
        log.info("CrawlerHandler Initialized: {}", this.getClass().getSimpleName());
        CrawlerType crawlerType = this.getCrawlerType();
        if (crawlerType == null) {
            log.warn("crawlerType handler without type needs to be checked! {}", this.getClass().getName());
            return;
        }
        HANDLER_MAP.put(crawlerType, this);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        CrawlerHandler.applicationContext = context;
    }

    protected abstract CrawlerType getCrawlerType();

    /**
     * 爬取網站並取得資料 (此方法不一定需要一次將爬取的資料全部回傳，可以邊爬取邊處理)
     *
     * @param driver        WebDriver，由父類管理生命週期
     * @param crawledData 爬蟲參數
     * @return 爬取的資料
     */
    protected abstract List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) throws Exception;


    /**
     * 從 ApplicationContext 取得 bean 的方法
     *
     * @param beanClass bean 的類別
     * @return bean 實例，若取不到則返回 null
     */
    private <T> T getBean(Class<T> beanClass) {
        if (applicationContext != null) {
            try {
                return applicationContext.getBean(beanClass);
            } catch (Exception e) {
                log.error("無法從 ApplicationContext 取得 bean: {}", beanClass.getName(), e);
            }
        }
        return null;
    }


}
