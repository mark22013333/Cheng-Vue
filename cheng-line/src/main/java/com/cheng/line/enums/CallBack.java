package com.cheng.line.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Cheng
 * @since 2025-10-30 00:10
 **/
@Getter
@AllArgsConstructor
public enum CallBack {

    WEBHOOK("/webhook/line");

    private final String url;
}
