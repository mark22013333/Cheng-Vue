package com.cheng.web.controller.sse;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.framework.sse.SseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * SSE 統一端點控制器
 * 提供統一的 SSE 訂閱、取消訂閱和監控功能
 * 
 * @author cheng
 */
@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {
    
    @Autowired
    private SseManager sseManager;
    
    /**
     * 訂閱 SSE (統一端點)
     * GET /sse/subscribe/{channel}/{taskId}
     * 
     * @param channel 頻道名稱
     * @param taskId 任務 ID
     * @param timeout 超時時間（毫秒），預設 30 分鐘
     * @return SseEmitter
     */
    @Anonymous
    @GetMapping(value = "/subscribe/{channel}/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String channel,
            @PathVariable String taskId,
            @RequestParam(required = false) Long timeout) {
        
        log.info("[SSE] 收到訂閱請求: channel={}, taskId={}, timeout={}", channel, taskId, timeout);
        return sseManager.subscribe(channel, taskId, timeout);
    }
    
    /**
     * 取消訂閱
     * DELETE /sse/unsubscribe/{channel}/{taskId}
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @return AjaxResult
     */
    @DeleteMapping("/unsubscribe/{channel}/{taskId}")
    public AjaxResult unsubscribe(
            @PathVariable String channel,
            @PathVariable String taskId) {
        
        log.info("[SSE] 收到取消訂閱請求: channel={}, taskId={}", channel, taskId);
        sseManager.unsubscribe(channel, taskId);
        return AjaxResult.success("取消訂閱成功");
    }
    
    /**
     * 取得 SSE 統計資訊
     * GET /sse/statistics
     * 
     * @return 統計資訊
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = sseManager.getStatistics();
        return AjaxResult.success(stats);
    }
    
    /**
     * 檢查連線是否存在
     * GET /sse/exists/{channel}/{taskId}
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @return AjaxResult
     */
    @GetMapping("/exists/{channel}/{taskId}")
    public AjaxResult exists(
            @PathVariable String channel,
            @PathVariable String taskId) {
        
        boolean exists = sseManager.exists(channel, taskId);
        return AjaxResult.success(exists);
    }
}
