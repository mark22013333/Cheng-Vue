package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopSearchLog;
import com.cheng.shop.domain.vo.PopularSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜尋記錄 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopSearchLogMapper {

    /**
     * 插入搜尋記錄
     *
     * @param log 搜尋記錄
     */
    void insert(ShopSearchLog log);

    /**
     * 統計熱門搜尋關鍵字（依搜尋次數排序）
     *
     * @param since 統計起始時間
     * @param limit 查詢數量上限
     * @return 熱門關鍵字列表
     */
    List<PopularSearchVO> selectPopularKeywords(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * 將訪客搜尋紀錄合併至會員
     *
     * @param guestId  訪客識別碼
     * @param memberId 會員ID
     */
    void mergeGuestToMember(@Param("guestId") String guestId, @Param("memberId") Long memberId);
}
