package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.crawler.dto.CrawlTaskDTO;
import com.cheng.crawler.service.ICrawlTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ISBN 爬取任務 Controller
 *
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/inventory/crawlTask")
@RequiredArgsConstructor
public class InvCrawlTaskController extends BaseController {
    
    private final ICrawlTaskService crawlTaskService;
    
    /**
     * SSE 連線管理
     * Key: taskId, Value: SseEmitter
     */
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    /**
     * 建立爬取任務
     *
     * @param isbn ISBN
     * @return 任務 ID
     */
    @PostMapping("/create")
    public AjaxResult createTask(@RequestParam String isbn) {
        log.info("收到建立爬取任務請求: isbn={}", isbn);
        
        try {
            String taskId = crawlTaskService.createTask(isbn);
            return AjaxResult.success("已將 ISBN 加入佇列", taskId);
        } catch (Exception e) {
            log.error("建立爬取任務失敗: isbn={}", isbn, e);
            return error(e.getMessage());
        }
    }
    
    /**
     * 查詢任務狀態
     *
     * @param taskId 任務 ID
     * @return 任務資訊
     */
    @GetMapping("/status/{taskId}")
    public AjaxResult getTaskStatus(@PathVariable String taskId) {
        CrawlTaskDTO task = crawlTaskService.getTask(taskId);
        
        if (task == null) {
            return error("任務不存在");
        }
        
        return success(task);
    }
    
    /**
     * 查詢所有進行中的任務
     *
     * @return 任務列表
     */
    @GetMapping("/active")
    public AjaxResult getActiveTasks() {
        List<CrawlTaskDTO> tasks = crawlTaskService.getActiveTasks();
        return success(tasks);
    }
    
    /**
     * 批次查詢任務狀態
     *
     * @param taskIds 任務 ID 列表（逗號分隔）
     * @return 任務列表
     */
    @GetMapping("/batchStatus")
    public AjaxResult getBatchTaskStatus(@RequestParam String taskIds) {
        String[] ids = taskIds.split(",");
        List<CrawlTaskDTO> tasks = Arrays.stream(ids)
                .map(crawlTaskService::getTask)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return success(tasks);
    }
    
    // ==================== SSE 訂閱 ====================
    
    /**
     * 訂閱任務狀態更新（SSE）
     *
     * @param taskId 任務 ID
     * @return SseEmitter
     */
    @Anonymous
    @GetMapping(value = "/subscribe/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String taskId) {
        log.info("收到 SSE 訂閱請求: taskId={}", taskId);
        
        // 建立 SseEmitter（逾時 30 分鐘）
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        // 儲存連線
        sseEmitters.put(taskId, emitter);
        
        // 設定完成/逾時/錯誤回調
        emitter.onCompletion(() -> {
            log.info("SSE 連線正常完成: taskId={}", taskId);
            sseEmitters.remove(taskId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE 連線逾時: taskId={}", taskId);
            sseEmitters.remove(taskId);
        });
        
        emitter.onError((e) -> {
            log.error("SSE 連線錯誤: taskId={}", taskId, e);
            sseEmitters.remove(taskId);
        });
        
        // 啟動輪詢任務狀態
        startPolling(taskId, emitter);
        
        return emitter;
    }
    
    /**
     * 輪詢任務狀態並推送
     *
     * @param taskId 任務 ID
     * @param emitter SseEmitter
     */
    private void startPolling(String taskId, SseEmitter emitter) {
        new Thread(() -> {
            try {
                while (sseEmitters.containsKey(taskId)) {
                    CrawlTaskDTO task = crawlTaskService.getTask(taskId);
                    
                    if (task == null) {
                        // 任務不存在
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"message\":\"任務不存在\"}"));
                        emitter.complete();
                        sseEmitters.remove(taskId);
                        break;
                    }
                    
                    // 推送任務狀態
                    emitter.send(SseEmitter.event()
                        .name("task-update")
                        .data(task));
                    
                    // 如果任務完成或失敗，結束推送
                    if (task.getStatus() == CrawlTaskDTO.TaskStatus.COMPLETED ||
                        task.getStatus() == CrawlTaskDTO.TaskStatus.FAILED) {
                        log.info("任務已完成，結束 SSE 推送: taskId={}, status={}", taskId, task.getStatus());
                        
                        // 等待一下確保前端收到最後一次更新
                        TimeUnit.MILLISECONDS.sleep(500);
                        
                        emitter.complete();
                        sseEmitters.remove(taskId);
                        break;
                    }
                    
                    // 每 2 秒檢查一次
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (IOException e) {
                log.error("SSE 推送失敗: taskId={}", taskId, e);
                sseEmitters.remove(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("SSE 輪詢被中斷: taskId={}", taskId);
                sseEmitters.remove(taskId);
            } catch (Exception e) {
                log.error("SSE 處理異常: taskId={}", taskId, e);
                sseEmitters.remove(taskId);
            }
        }).start();
    }
    
    /**
     * 取消訂閱
     *
     * @param taskId 任務 ID
     */
    @DeleteMapping("/unsubscribe/{taskId}")
    public void unsubscribe(@PathVariable String taskId) {
        SseEmitter emitter = sseEmitters.remove(taskId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("取消 SSE 訂閱: taskId={}", taskId);
            } catch (Exception e) {
                log.error("取消 SSE 訂閱失敗: taskId={}", taskId, e);
            }
        }
    }
}
