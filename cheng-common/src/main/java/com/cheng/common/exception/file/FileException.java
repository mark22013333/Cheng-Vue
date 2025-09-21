package com.cheng.common.exception.file;

import com.cheng.common.exception.base.BaseException;

import java.io.Serial;

/**
 * 文件訊息異常類
 *
 * @author cheng
 */
public class FileException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }

}
