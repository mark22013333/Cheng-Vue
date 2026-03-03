package com.cheng.shop.security;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.constant.Constants;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.AddressUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 商城會員 token 驗證處理
 *
 * @author cheng
 */
@Component
public class MemberTokenService {
    private static final Logger log = LoggerFactory.getLogger(MemberTokenService.class);

    @Value("${member-token.header}")
    private String header;

    @Value("${member-token.secret}")
    private String secret;

    @Value("${member-token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 取得登入會員資訊
     * <p>
     * 包含密碼變更後的 JWT 強制失效檢查：
     * 若 Redis 中存在 {@code member_pwd_changed:{memberId}} 且時間戳晚於 token 建立時間，
     * 表示密碼已變更，此 token 應被視為無效。
     */
    public ShopMemberLoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                ShopMemberLoginUser loginUser = redisCache.getCacheObject(userKey);

                // 密碼變更後的 JWT 強制失效檢查
                if (loginUser != null && isTokenInvalidatedByPasswordChange(loginUser, userKey)) {
                    return null;
                }

                return loginUser;
            } catch (Exception e) {
                log.error("取得會員資訊異常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 檢查 token 是否因密碼變更而失效
     * <p>
     * 比對 loginTime 與 Redis 中的密碼變更時間戳
     */
    private boolean isTokenInvalidatedByPasswordChange(ShopMemberLoginUser loginUser, String userKey) {
        if (loginUser.getMemberId() == null) {
            return false;
        }
        Long pwdChangedTime = redisCache.getCacheObject(
                CacheConstants.MEMBER_PWD_CHANGED_KEY + loginUser.getMemberId());
        if (pwdChangedTime != null && loginUser.getLoginTime() < pwdChangedTime) {
            log.info("會員 {} 的 token 因密碼變更而失效（loginTime={}, pwdChanged={}）",
                    loginUser.getMemberId(), loginUser.getLoginTime(), pwdChangedTime);
            redisCache.deleteObject(userKey);
            return true;
        }
        return false;
    }

    /**
     * 設定登入會員資訊
     */
    public void setLoginUser(ShopMemberLoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刪除登入會員資訊
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 建立 token
     */
    public String createToken(ShopMemberLoginUser loginUser) {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    /**
     * 驗證 token
     */
    public void verifyToken(ShopMemberLoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY) {
            refreshToken(loginUser);
        }
    }

    /**
     * 重新整理 token 有效期
     */
    public void refreshToken(ShopMemberLoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 設定使用者代理資訊
     */
    public void setUserAgent(ShopMemberLoginUser loginUser) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 取得請求 token
     */
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return CacheConstants.MEMBER_LOGIN_TOKEN_KEY + uuid;
    }
}
