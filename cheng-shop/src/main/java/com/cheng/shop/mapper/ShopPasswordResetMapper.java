package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopPasswordReset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 密碼重設 Token Mapper
 *
 * @author cheng
 */
@Mapper
public interface ShopPasswordResetMapper {

    /**
     * 透過 selector 查詢記錄（不限 used / expires_at，由 service 層判斷）
     *
     * @param selector 選擇器
     * @return 密碼重設記錄
     */
    ShopPasswordReset selectBySelector(String selector);

    /**
     * 插入新的密碼重設記錄
     *
     * @param record 密碼重設記錄
     * @return 影響行數
     */
    int insert(ShopPasswordReset record);

    /**
     * 標記為已使用
     *
     * @param id     記錄 ID
     * @param usedAt 使用時間
     * @return 影響行數
     */
    int markAsUsed(@Param("id") Long id, @Param("usedAt") Date usedAt);

    /**
     * 原子消耗 token（僅在未使用、未過期且 hashed_token 匹配時成功）
     *
     * @param id          記錄 ID
     * @param hashedToken SHA-256(token)
     * @param usedAt      使用時間
     * @return 影響行數（1=成功消耗，0=已失效/已使用/不匹配）
     */
    int consumeToken(@Param("id") Long id,
                     @Param("hashedToken") String hashedToken,
                     @Param("usedAt") Date usedAt);

    /**
     * 將同一 email 所有未使用的記錄標記為已使用（發送新連結前呼叫）
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
