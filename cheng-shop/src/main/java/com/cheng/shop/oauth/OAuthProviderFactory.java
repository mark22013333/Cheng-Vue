package com.cheng.shop.oauth;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.enums.SocialProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth 策略工廠
 * <p>
 * 自動收集所有 {@link OAuthProviderStrategy} Bean，
 * 根據 {@link SocialProvider} 路由到對應的策略實作。
 * </p>
 *
 * @author cheng
 */
@Component
public class OAuthProviderFactory {

    private final Map<SocialProvider, OAuthProviderStrategy> strategyMap;

    public OAuthProviderFactory(List<OAuthProviderStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(OAuthProviderStrategy::getProvider, Function.identity()));
    }

    /**
     * 取得指定平台的策略實作
     *
     * @param provider 第三方平台
     * @return 策略實作
     * @throws ServiceException 不支援的平台
     */
    public OAuthProviderStrategy getProvider(SocialProvider provider) {
        OAuthProviderStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new ServiceException("不支援的登入方式：" + provider.getDescription());
        }
        return strategy;
    }
}
