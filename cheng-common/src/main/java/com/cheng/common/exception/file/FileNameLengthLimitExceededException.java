package com.cheng.common.exception.file;

import java.io.Serial;

/**
 * 檔案名稱超長限制異常類
 *
 * @author cheng
 */
public class FileNameLengthLimitExceededException extends FileException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength) {
        super("upload.filename.exceed.length", new Object[]{defaultFileNameLength});
    }
}
