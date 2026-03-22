package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopBrowsingLog;
import com.cheng.shop.domain.vo.HotProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品瀏覽記錄 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopBrowsingLogMapper {

    /**
     * 插入瀏覽記錄
     *
     * @param log 瀏覽記錄
     */
    void insert(ShopBrowsingLog log);

    /**
     * 查詢會員最近瀏覽記錄（同一商品去重，只取最近一次）
     *
     * @param memberId 會員ID
     * @param limit    查詢數量上限
     * @return 瀏覽記錄列表
     */
    List<ShopBrowsingLog> selectRecentByMemberId(@Param("memberId") Long memberId, @Param("limit") int limit);

    /**
     * 統計熱門商品（依瀏覽次數排序）
     *
     * @param since 統計起始時間
     * @param limit 查詢數量上限
     * @return 熱門商品列表
     */
    List<HotProductVO> selectHotProducts(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * 將訪客瀏覽紀錄合併至會員
     *
     * @param guestId  訪客識別碼
     * @param memberId 會員ID
     */
    void mergeGuestToMember(@Param("guestId") String guestId, @Param("memberId") Long memberId);
}
