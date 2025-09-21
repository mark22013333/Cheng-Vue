package com.cheng.common.enums;

import com.cheng.common.utils.DesensitizedUtil;

import java.util.function.Function;

/**
 * 去識別化類型
 *
 * @author cheng
 */
public enum DesensitizedType {
    /**
     * 姓名，第2位星號替換
     */
    USERNAME(s -> s.replaceAll("(\\S)\\S(\\S*)", "$1*$2")),

    /**
     * 密碼，全部字串都用*代替
     */
    PASSWORD(DesensitizedUtil::password),

    /**
     * 身份證，中間10位星號替換
     */
    ID_CARD(s -> s.replaceAll("(\\d{4})\\d{10}(\\d{3}[Xx]|\\d{4})", "$1** **** ****$2")),

    /**
     * 手機號，中間4位星號替換
     */
    PHONE(s -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")),

    /**
     * 電子信箱，僅顯示第一個字母和@後面的地址顯示，其他星號替換
     */
    EMAIL(s -> s.replaceAll("(^.)[^@]*(@.*$)", "$1****$2")),

    /**
     * 銀行卡號，保留最後4位，其他星號替換
     */
    BANK_CARD(s -> s.replaceAll("\\d{15}(\\d{3})", "**** **** **** **** $1")),

    /**
     * 車牌號碼，包含普通車輛、新能源車輛
     */
    CAR_LICENSE(DesensitizedUtil::carLicense);

    private final Function<String, String> desensitizer;

    DesensitizedType(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
