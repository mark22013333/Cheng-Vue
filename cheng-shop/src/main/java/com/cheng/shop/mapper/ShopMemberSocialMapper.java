package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopMemberSocial;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 會員第三方登入綁定 Mapper
 *
 * @author cheng
 */
public interface ShopMemberSocialMapper {

    /**
     * 根據平台和平台用戶 ID 查詢綁定記錄
     *
     * @param provider   第三方平台
     * @param providerId 平台用戶唯一 ID
     * @return 綁定記錄
     */
    ShopMemberSocial selectByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId);

    /**
     * 根據會員 ID 查詢所有綁定記錄
     *
     * @param memberId 會員 ID
     * @return 綁定記錄列表
     */
    List<ShopMemberSocial> selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 新增綁定記錄
     *
     * @param social 綁定實體
     * @return 影響行數
     */
    int insertSocial(ShopMemberSocial social);

    /**
     * 刪除綁定記錄（解綁）
     *
     * @param memberId 會員 ID
     * @param provider 第三方平台
     * @return 影響行數
     */
    int deleteSocial(@Param("memberId") Long memberId, @Param("provider") String provider);

    /**
     * 更新 Token 資訊
     *
     * @param social 包含新 Token 的綁定實體
     * @return 影響行數
     */
    int updateTokens(ShopMemberSocial social);
}
