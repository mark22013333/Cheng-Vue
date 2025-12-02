package com.cheng.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author cheng
 * @since 2024/5/13 22:29
 **/
@UtilityClass
public class TraceUtils {
    private static final int TRACE_ID_LENGTH = 12;
    private static final char[] CHAR_MAPPING = initCharMapping();

    private static char[] initCharMapping() {
        char[] mapping = new char[62];  // 只包含 0-9, A-Z, a-z，共62個
        int index = 0;
        for (char c = '0'; c <= '9'; c++) {
            mapping[index++] = c;
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            mapping[index++] = c;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            mapping[index++] = c;
        }
        return mapping;
    }

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 初始化traceId
     */
    public static String initTrace() {
        return initTrace(null, null, TRACE_ID);
    }

    public static String initSpan() {
        return initTrace(null, null, SPAN_ID);
    }

    /**
     * 初始化traceId
     */
    public static String initTrace(String channelId) {
        return initTrace(channelId, null, TRACE_ID);
    }

    /**
     * 初始化traceId
     */
    public static String initTrace(String channelId, String traceId, String type) {
        if (StringUtils.isNotBlank(channelId)) {
            channelId += "_";
        } else {
            channelId = "";
        }
        traceId = Optional.ofNullable(traceId).orElse(generateTraceId());

        switch (type) {
            case SPAN_ID -> setSpanId(traceId);
            case TRACE_ID -> setTraceId(channelId + traceId);
            default -> throw new IllegalArgumentException("trace type error");
        }

        return traceId;
    }

    /**
     * 從MDC中移除traceId
     */
    public static void clearTrace() {
        MDC.remove(TRACE_ID);
    }

    public static void clearSpan() {
        MDC.remove(SPAN_ID);
    }

    /**
     * 從MDC中取得traceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 將traceId放到MDC
     *
     * @param traceId traceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    private static void setSpanId(String spanId) {
        MDC.put(SPAN_ID, spanId);
    }

    /**
     * 產生traceId
     *
     * @return traceId
     */
    public static String generateTraceId() {
        byte[] bytes = new byte[TRACE_ID_LENGTH];
        ThreadLocalRandom.current().nextBytes(bytes);
        StringBuilder traceId = new StringBuilder(TRACE_ID_LENGTH);

        for (int i = 0; i < TRACE_ID_LENGTH; i++) {
            int index = (bytes[i] & 0xFF) % CHAR_MAPPING.length;
            traceId.append(CHAR_MAPPING[index]);
        }

        return traceId.toString();
    }

    public static void main(String[] args) {
        HashSet<String> set = new HashSet<>();
        long startTime = System.currentTimeMillis();
        int printInterval = 100000;

        for (int i = 0; i < 10000000; i++) {
            String s = generateTraceId();

            if (set.contains(s)) {
                System.out.println("Duplicate traceId: " + s);
                System.out.println("i = " + i);
                break;
            } else {
                if (i % printInterval == 0) {
                    System.out.println("s = " + s + ", i = " + i);
                }
                set.add(s);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total time:" + (endTime - startTime) + " ms");
    }
}
