package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopEmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * Email 驗證 Token Mapper
 *
 * @author cheng
 */
@Mapper
public interface ShopEmailVerificationMapper {

    /**
     * 透過 selector 查詢記錄（不限 used / expires_at，由 service 層判斷）
     *
     * @param selector 選擇器
     * @return Email 驗證記錄
     */
    ShopEmailVerification selectBySelector(String selector);

    /**
     * 插入新的 Email 驗證記錄
     *
     * @param record Email 驗證記錄
     * @return 影響行數
     */
    int insert(ShopEmailVerification record);

    /**
     * 標記為已使用
     *
     * @param id     記錄 ID
     * @param usedAt 使用時間
     * @return 影響行數
     */
    int markAsUsed(@Param("id") Long id, @Param("usedAt") Date usedAt);

    /**
     * 將同一會員所有未使用的記錄標記為已使用（發送新驗證信前呼叫）
     *
     * @param memberId 會員 ID
     * @return 影響行數
     */
    int invalidateByMemberId(Long memberId);

    /**
     * 將同一 email 所有未使用的記錄標記為已使用
     *
     * @param email 電子信箱
     * @return 影響行數
     */
    int invalidateByEmail(String email);

    /**
     * 刪除已過期記錄（排程清理用）
     *
     * @return 刪除行數
     */
    int deleteExpired();
}
