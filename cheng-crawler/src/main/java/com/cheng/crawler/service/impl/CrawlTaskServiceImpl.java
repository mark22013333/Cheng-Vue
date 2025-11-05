package com.cheng.crawler.service.impl;

import com.cheng.crawler.dto.CrawlTaskDTO;
import com.cheng.crawler.service.ICrawlTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 爬取任務服務實現
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlTaskServiceImpl implements ICrawlTaskService {
    
    private final CrawlTaskExecutor taskExecutor;
    
    /**
     * 任務存儲 Map（記憶體儲存）
     * Key: taskId, Value: CrawlTaskDTO
     */
    private final Map<String, CrawlTaskDTO> taskMap = new ConcurrentHashMap<>();
    
    /**
     * 最大任務數量限制
     */
    private static final int MAX_TASKS = 100;
    
    /**
     * 任務過期時間（分鐘）
     */
    private static final int TASK_EXPIRE_MINUTES = 30;
    
    @Override
    public String createTask(String isbn) {
        // 清理過期任務
        cleanExpiredTasks();
        
        // 檢查任務數量限制
        if (taskMap.size() >= MAX_TASKS) {
            throw new RuntimeException("任務數量已達上限，請稍後再試");
        }
        
        // 建立任務
        String taskId = UUID.randomUUID().toString();
        CrawlTaskDTO task = new CrawlTaskDTO();
        task.setTaskId(taskId);
        task.setIsbn(isbn);
        task.setStatus(CrawlTaskDTO.TaskStatus.PENDING);
        task.setCreateTime(LocalDateTime.now());
        
        taskMap.put(taskId, task);
        
        log.info("建立爬取任務: taskId={}, isbn={}", taskId, isbn);
        
        // 非同步執行任務（透過獨立的 Executor 以確保 @Async 生效）
        taskExecutor.executeTask(taskId, taskMap);
        
        return taskId;
    }
    
    @Override
    public CrawlTaskDTO getTask(String taskId) {
        return taskMap.get(taskId);
    }
    
    @Override
    public List<CrawlTaskDTO> getActiveTasks() {
        return taskMap.values().stream()
                .filter(task -> task.getStatus() == CrawlTaskDTO.TaskStatus.PENDING ||
                               task.getStatus() == CrawlTaskDTO.TaskStatus.PROCESSING)
                .collect(Collectors.toList());
    }
    
    /**
     * 清理過期任務
     */
    private void cleanExpiredTasks() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(TASK_EXPIRE_MINUTES);
        
        taskMap.entrySet().removeIf(entry -> {
            CrawlTaskDTO task = entry.getValue();
            boolean shouldRemove = task.getCreateTime().isBefore(expireTime) &&
                    (task.getStatus() == CrawlTaskDTO.TaskStatus.COMPLETED ||
                     task.getStatus() == CrawlTaskDTO.TaskStatus.FAILED);
            
            if (shouldRemove) {
                log.debug("清理過期任務: taskId={}, isbn={}", task.getTaskId(), task.getIsbn());
            }
            
            return shouldRemove;
        });
    }
}
