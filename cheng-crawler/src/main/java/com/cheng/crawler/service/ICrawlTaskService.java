package com.cheng.crawler.service;

import com.cheng.crawler.dto.CrawlTaskDTO;

import java.util.List;

/**
 * 爬取任務服務介面
 *
 * @author cheng
 */
public interface ICrawlTaskService {

    /**
     * 建立爬取任務
     *
     * @param isbn ISBN
     * @return 任務 ID
     */
    String createTask(String isbn);

    /**
     * 取得任務狀態
     *
     * @param taskId 任務 ID
     * @return 任務 DTO
     */
    CrawlTaskDTO getTask(String taskId);

    /**
     * 取得使用者的所有進行中任務
     *
     * @return 任務列表
     */
    List<CrawlTaskDTO> getActiveTasks();
}
