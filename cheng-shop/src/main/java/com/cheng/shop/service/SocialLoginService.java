package com.cheng.shop.service;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.domain.ShopMemberSocial;
import com.cheng.shop.domain.dto.OAuthTokenResponse;
import com.cheng.shop.domain.dto.OAuthUserProfile;
import com.cheng.shop.enums.MemberStatus;
import com.cheng.shop.enums.SocialProvider;
import com.cheng.shop.mapper.ShopMemberSocialMapper;
import com.cheng.shop.oauth.LineOAuthProvider;
import com.cheng.shop.oauth.OAuthProviderFactory;
import com.cheng.shop.oauth.OAuthProviderStrategy;
import com.cheng.shop.security.MemberTokenService;
import com.cheng.shop.security.ShopMemberLoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 社交登入編排服務
 * <p>
 * 負責 OAuth 流程的完整編排：
 * <ul>
 *   <li>產生授權 URL + 存 state 到 Redis</li>
 *   <li>處理回調：驗證 state → 換 token → 取 profile → 查/建會員 → 產 JWT</li>
 *   <li>綁定/解綁社交帳號</li>
 *   <li>查詢綁定狀態</li>
 * </ul>
 * </p>
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final OAuthProviderFactory providerFactory;
    private final ShopMemberSocialMapper socialMapper;
    private final IShopMemberService memberService;
    private final MemberTokenService memberTokenService;
    private final ShopConfigService configService;
    private final LineOAuthProvider lineOAuthProvider;
    private final RedisCache redisCache;

    /** OAuth state 有效期（5 分鐘） */
    private static final int STATE_TTL_MINUTES = 5;

    /**
     * 產生授權 URL + 存 state 到 Redis
     *
     * @param provider    第三方平台
     * @param redirectUri OAuth 回調 URI
     * @return 完整的授權 URL
     */
    public String getAuthorizeUrl(SocialProvider provider, String redirectUri) {
        // 檢查平台是否啟用
        checkProviderEnabled(provider);

        OAuthProviderStrategy strategy = providerFactory.getProvider(provider);

        // 產生 state（CSRF 防護）
        String state = IdUtils.fastUUID();
        String stateKey = CacheConstants.OAUTH_STATE_KEY + state;
        redisCache.setCacheObject(stateKey, provider.getCode(), STATE_TTL_MINUTES, TimeUnit.MINUTES);

        return strategy.buildAuthorizeUrl(state, redirectUri);
    }

    /**
     * 處理 OAuth 回調
     * <p>
     * 完整流程：驗證 state → 換 token → 取 profile → 查/建會員 → 產 JWT
     * </p>
     *
     * @param provider    第三方平台
     * @param code        授權碼
     * @param state       CSRF state
     * @param redirectUri OAuth 回調 URI
     * @param request     HTTP 請求（取 IP）
     * @return JWT token
     */
    @Transactional
    public String handleCallback(SocialProvider provider, String code,
                                 String state, String redirectUri, HttpServletRequest request) {
        // 1. 驗證 state（一次性使用）
        validateState(state, provider);

        // 2. 換 token
        OAuthProviderStrategy strategy = providerFactory.getProvider(provider);
        OAuthTokenResponse tokenResponse = strategy.exchangeToken(code, redirectUri);

        // 3. 取 user profile
        OAuthUserProfile profile = strategy.getUserProfile(tokenResponse.getAccessToken());
        log.info("OAuth 回調：provider={}, providerId={}, nickname={}",
                provider.getCode(), profile.getProviderId(), profile.getNickname());

        // 4. 查找已綁定的會員
        ShopMemberSocial existingSocial = socialMapper.selectByProviderAndProviderId(
                provider.getCode(), profile.getProviderId());

        ShopMember member;
        if (existingSocial != null) {
            // 已綁定 → 取出會員
            member = memberService.selectMemberById(existingSocial.getMemberId());
            if (member == null) {
                throw new ServiceException("綁定的會員已不存在");
            }

            // 檢查會員狀態
            MemberStatus memberStatus = member.getStatusEnum();
            if (memberStatus == null || !memberStatus.canLogin()) {
                throw new ServiceException("此帳號已停用或凍結");
            }

            // 更新 Token
            updateSocialTokens(existingSocial, tokenResponse, profile);
        } else {
            // 未綁定 → 自動建立新會員
            member = createMemberFromOAuth(profile, provider);

            // 建立綁定記錄
            createSocialBinding(member.getMemberId(), provider, profile, tokenResponse);
        }

        // 5. 更新登入資訊 + 產 JWT
        memberService.updateLoginInfo(member.getMemberId(), IpUtils.getIpAddr());
        ShopMemberLoginUser loginUser = new ShopMemberLoginUser(member);
        return memberTokenService.createToken(loginUser);
    }

    /**
     * 綁定社交帳號到已登入會員
     */
    @Transactional
    public void linkAccount(Long memberId, SocialProvider provider,
                            String code, String redirectUri) {
        checkProviderEnabled(provider);
        OAuthProviderStrategy strategy = providerFactory.getProvider(provider);

        // 換 token + 取 profile
        OAuthTokenResponse tokenResponse = strategy.exchangeToken(code, redirectUri);
        OAuthUserProfile profile = strategy.getUserProfile(tokenResponse.getAccessToken());

        // 檢查是否已被其他會員綁定
        ShopMemberSocial existing = socialMapper.selectByProviderAndProviderId(
                provider.getCode(), profile.getProviderId());
        if (existing != null) {
            if (existing.getMemberId().equals(memberId)) {
                throw new ServiceException("您已綁定此 " + provider.getDescription() + " 帳號");
            }
            throw new ServiceException("此 " + provider.getDescription() + " 帳號已被其他會員綁定");
        }

        // 檢查會員是否已綁定同平台
        List<ShopMemberSocial> memberSocials = socialMapper.selectByMemberId(memberId);
        boolean alreadyLinked = memberSocials.stream()
                .anyMatch(s -> s.getProvider().equals(provider.getCode()));
        if (alreadyLinked) {
            throw new ServiceException("您已綁定 " + provider.getDescription() + " 帳號，請先解綁");
        }

        createSocialBinding(memberId, provider, profile, tokenResponse);
        log.info("會員 {} 綁定 {} 帳號成功，providerId={}", memberId, provider.getCode(), profile.getProviderId());
    }

    /**
     * 解除綁定
     */
    @Transactional
    public void unlinkAccount(Long memberId, SocialProvider provider) {
        // 檢查會員是否有本地密碼，若無則不允許解綁最後一個社交帳號
        ShopMember member = memberService.selectMemberById(memberId);
        if (member == null) {
            throw new ServiceException("會員不存在");
        }

        List<ShopMemberSocial> socials = socialMapper.selectByMemberId(memberId);
        boolean hasPassword = member.getPassword() != null && !member.getPassword().isBlank();
        boolean hasSingleSocial = socials.size() <= 1;

        if (!hasPassword && hasSingleSocial) {
            throw new ServiceException("您尚未設定密碼，無法解除最後一個社交帳號綁定");
        }

        int rows = socialMapper.deleteSocial(memberId, provider.getCode());
        if (rows <= 0) {
            throw new ServiceException("未找到此平台的綁定記錄");
        }
        log.info("會員 {} 解綁 {} 帳號成功", memberId, provider.getCode());
    }

    /**
     * 查詢會員的社交帳號綁定狀態
     */
    public List<Map<String, Object>> getBindings(Long memberId) {
        List<ShopMemberSocial> socials = socialMapper.selectByMemberId(memberId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SocialProvider provider : SocialProvider.values()) {
            Map<String, Object> binding = new HashMap<>(4);
            binding.put("provider", provider.getCode());
            binding.put("description", provider.getDescription());

            Optional<ShopMemberSocial> matched = socials.stream()
                    .filter(s -> s.getProvider().equals(provider.getCode()))
                    .findFirst();

            binding.put("linked", matched.isPresent());
            matched.ifPresent(s -> {
                binding.put("nickname", s.getNickname());
                binding.put("avatar", s.getAvatar());
                binding.put("bindTime", s.getCreateTime());
            });

            result.add(binding);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 驗證 OAuth state（一次性使用，防 CSRF + 重放攻擊）
     */
    private void validateState(String state, SocialProvider provider) {
        String stateKey = CacheConstants.OAUTH_STATE_KEY + state;
        String storedProvider = redisCache.getCacheObject(stateKey);

        if (storedProvider == null) {
            throw new ServiceException("登入請求已過期，請重新操作");
        }

        // 驗證 provider 一致
        if (!provider.getCode().equals(storedProvider)) {
            throw new ServiceException("登入平台不一致");
        }

        // 一次性使用：刪除 state
        redisCache.deleteObject(stateKey);
    }

    /**
     * 檢查平台是否啟用
     * <p>
     * LINE：檢查預設 LINE 頻道是否已設定 Login Channel ID/Secret
     * Google：檢查 sys_config 中的啟用開關
     * </p>
     */
    private void checkProviderEnabled(SocialProvider provider) {
        switch (provider) {
            case LINE -> {
                if (!lineOAuthProvider.isLoginConfigured()) {
                    throw new ServiceException("LINE 登入尚未啟用，請至「行銷管理 → LINE 設定」配置 Login Channel");
                }
            }
            case GOOGLE -> {
                if (!configService.getBoolean(ShopConfigKey.OAUTH_GOOGLE_ENABLED)) {
                    throw new ServiceException(provider.getDescription() + " 登入尚未啟用");
                }
            }
            default -> throw new ServiceException("不支援的登入方式：" + provider.getDescription());
        }
    }

    /**
     * 從 OAuth profile 自動建立新會員
     */
    private ShopMember createMemberFromOAuth(OAuthUserProfile profile, SocialProvider provider) {
        ShopMember member = new ShopMember();
        member.setNickname(profile.getNickname() != null ? profile.getNickname() : "用戶");
        member.setAvatar(profile.getAvatarUrl());
        member.setEmail(profile.getEmail());
        member.setEmailVerified(profile.getEmail() != null);
        member.setStatus(MemberStatus.ACTIVE.getCode());
        // password 保持 null（第三方登入免密碼）

        int rows = memberService.registerMember(member);
        if (rows <= 0) {
            throw new ServiceException("自動註冊失敗，請稍後再試");
        }

        log.info("透過 {} 自動建立會員：memberId={}, nickname={}",
                provider.getCode(), member.getMemberId(), member.getNickname());
        return member;
    }

    /**
     * 建立社交帳號綁定記錄
     */
    private void createSocialBinding(Long memberId, SocialProvider provider,
                                     OAuthUserProfile profile, OAuthTokenResponse tokenResponse) {
        ShopMemberSocial social = new ShopMemberSocial();
        social.setMemberId(memberId);
        social.setProvider(provider.getCode());
        social.setProviderId(profile.getProviderId());
        social.setUnionId(profile.getUnionId());
        social.setAccessToken(tokenResponse.getAccessToken());
        social.setRefreshToken(tokenResponse.getRefreshToken());
        social.setNickname(profile.getNickname());
        social.setAvatar(profile.getAvatarUrl());

        if (tokenResponse.getExpiresIn() != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, tokenResponse.getExpiresIn());
            social.setTokenExpire(cal.getTime());
        }

        socialMapper.insertSocial(social);
    }

    /**
     * 更新已綁定記錄的 Token 資訊
     */
    private void updateSocialTokens(ShopMemberSocial existing,
                                    OAuthTokenResponse tokenResponse,
                                    OAuthUserProfile profile) {
        existing.setAccessToken(tokenResponse.getAccessToken());
        existing.setRefreshToken(tokenResponse.getRefreshToken());
        existing.setNickname(profile.getNickname());
        existing.setAvatar(profile.getAvatarUrl());

        if (tokenResponse.getExpiresIn() != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, tokenResponse.getExpiresIn());
            existing.setTokenExpire(cal.getTime());
        }

        socialMapper.updateTokens(existing);
    }
}
