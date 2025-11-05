package com.cheng.common.utils;

import com.cheng.common.enums.ApiResult;
import com.cheng.common.enums.Uri;
import com.cheng.common.exception.base.BizException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cheng
 * @since 2023/12/3 12:02 PM
 **/
@Slf4j
@UtilityClass
public class ApiUtils {

    private static final int USABLE = -1;
    private static final Map<String, LinkedHashMap<Integer, LocalDateTime>> TYPE_MAP = new ConcurrentHashMap<>();

    /**
     * 控制API使用限制
     *
     * @param type        自訂，辨別此次API使用次數
     * @param usableLimit 可使用次數
     * @param minAfterUse 多少分後才可使用
     * @return 剩餘多少分可使用，-1則表示可使用
     **/
    public static int notCallApi(String type, int usableLimit, int minAfterUse) {
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, LocalDateTime> useMap = TYPE_MAP.computeIfAbsent(type, k -> new LinkedHashMap<>());
        int count = useMap.size();

        if (count >= usableLimit) {
            LocalDateTime beforeTime = useMap.get(usableLimit);
            LocalDateTime end = beforeTime.plusMinutes(minAfterUse);
            Duration duration = Duration.between(now, end);
            int min = Math.toIntExact(duration.toMinutes()) + 1;
            if (duration.isNegative()) {
                resetTypeMap(type, now);
                return USABLE;
            } else {
                return min;
            }
        } else {
            useMap.put(++count, now);
        }

        log.info("typeMap = {}", TYPE_MAP);
        return USABLE;
    }

    /**
     * 重置特定類型的使用記錄
     *
     * @param type 自訂，辨別此次API使用次數
     * @param now  當前時間
     */
    private static void resetTypeMap(String type, LocalDateTime now) {
        LinkedHashMap<Integer, LocalDateTime> newMap = new LinkedHashMap<>();
        newMap.put(1, now);
        TYPE_MAP.put(type, newMap);
    }

    public static String getUrl(String url, Uri uri) {
        return getUrl(url, uri.getUri());
    }

    public static String getUrl(String url, Uri uri, Object... args) {
        int placeholderCount = countPlaceholders(uri.getUri());

        // 檢查參數數量是否正確
        if (placeholderCount > 0 && args.length != placeholderCount) {
            throw BizException.create(ApiResult.PARAM_ERROR,
                    String.format("Expected %d arguments but found %d", placeholderCount, args.length));
        }
        return getUrl(url, String.format(uri.getUri(), args));
    }

    public static String getUrl(String url, String uri) {
        if (StringUtils.isEmpty(url)) {
            throw BizException.create(ApiResult.PARAM_ERROR, "url not found");
        }

        // all url like http://www.domain.com
        // not "/" at the last string
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // all uri like path/to/call
        // no "/" at the first string
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        // combine url and uri as http://www.domain.com/path/to/call
        return String.format("%s/%s", url, uri);
    }

    public static String genRandValue() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 使用UTF-8將參數編碼
     *
     * @param params 參數
     * @return 編碼後字串
     **/
    public static String encodingParams(Map<String, ?> params) {
        if (params == null) {
            return "";
        }
        return params.entrySet().stream()
                .map(e -> {
                    try {
                        String value = e.getValue() != null ? (String) e.getValue() : "";
                        return String.format("%s=%s", e.getKey(), URLEncoder.encode(value, StandardCharsets.UTF_8));
                    } catch (Exception ex) {
                        log.error("ERROR:", ex);
                        return null;
                    }
                })
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.joining("&"));
    }

    private static int countPlaceholders(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        return input.split("%s", -1).length - 1;
    }
}
