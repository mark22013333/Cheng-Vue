package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 掃描類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ScanType implements CodedEnum<String> {

    /**
     * 條碼
     */
    BARCODE("1", "條碼"),

    /**
     * QR Code
     */
    QRCODE("2", "QR Code");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得掃描類型
     *
     * @param code 類型代碼
     * @return 掃描類型
     */
    public static ScanType fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ScanType.class, code);
    }

    /**
     * 判斷是否為條碼
     *
     * @return true=條碼, false=其他
     */
    public boolean isBarcode() {
        return this == BARCODE;
    }

    /**
     * 判斷是否為 QR Code
     *
     * @return true=QR Code, false=其他
     */
    public boolean isQrcode() {
        return this == QRCODE;
    }
}
