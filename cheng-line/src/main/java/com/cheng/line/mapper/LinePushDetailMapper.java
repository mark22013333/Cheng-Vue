package com.cheng.line.mapper;

import com.cheng.line.domain.LinePushDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LINE 逐人推播明細 Mapper 介面
 *
 * @author cheng
 */
public interface LinePushDetailMapper {

    /**
     * 新增推播明細
     *
     * @param detail 推播明細
     * @return 結果
     */
    int insertDetail(LinePushDetail detail);

    /**
     * 批次新增推播明細
     *
     * @param details 推播明細列表
     * @return 結果
     */
    int insertBatch(@Param("details") List<LinePushDetail> details);

    /**
     * 根據訊息日誌 ID 查詢明細列表
     *
     * @param messageId 訊息日誌 ID
     * @return 推播明細集合
     */
    List<LinePushDetail> selectByMessageId(Long messageId);

    /**
     * 統計指定訊息日誌 ID 和狀態的明細數量
     *
     * @param messageId 訊息日誌 ID
     * @param status    狀態代碼
     * @return 數量
     */
    int countByMessageIdAndStatus(@Param("messageId") Long messageId, @Param("status") String status);

    /**
     * 更新推播明細狀態
     *
     * @param detail 推播明細（需包含 detailId）
     * @return 結果
     */
    int updateStatus(LinePushDetail detail);
}
