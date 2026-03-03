package com.cheng.shop.task;

import com.cheng.shop.mapper.ShopEmailVerificationMapper;
import com.cheng.shop.mapper.ShopPasswordResetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 商城安全維護任務
 *
 * <p>供 Quartz 呼叫，定期清理已過期安全 token 資料。</p>
 */
@Slf4j
@Component("shopSecurityTask")
@RequiredArgsConstructor
public class ShopSecurityTask {

    private final ShopPasswordResetMapper passwordResetMapper;
    private final ShopEmailVerificationMapper emailVerificationMapper;

    /**
     * 清理過期 token
     *
     * <p>Quartz invokeTarget：shopSecurityTask.cleanupExpiredTokens</p>
     */
    public void cleanupExpiredTokens() {
        try {
            int deletedPwdReset = passwordResetMapper.deleteExpired();
            int deletedEmailVerify = emailVerificationMapper.deleteExpired();
            log.info("商城安全清理完成：passwordResetDeleted={}, emailVerifyDeleted={}",
                    deletedPwdReset, deletedEmailVerify);
        } catch (Exception e) {
            log.error("商城安全清理任務執行失敗", e);
        }
    }
}
