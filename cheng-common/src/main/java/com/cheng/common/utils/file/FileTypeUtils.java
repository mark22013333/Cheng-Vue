package com.cheng.common.utils.file;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 檔案類型工具類
 *
 * @author cheng
 */
public class FileTypeUtils {
    /**
     * 取得檔案類型
     * <p>
     * 例如: cheng.txt, 返回: txt
     *
     * @param file 檔案名
     * @return 後綴（不含".")
     */
    public static String getFileType(File file) {
        if (null == file) {
            return StringUtils.EMPTY;
        }
        return getFileType(file.getName());
    }

    /**
     * 取得檔案類型
     * <p>
     * 例如: cheng.txt, 返回: txt
     *
     * @param fileName 檔案名
     * @return 後綴（不含".")
     */
    public static String getFileType(String fileName) {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0) {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase();
    }

    /**
     * 取得檔案類型
     *
     * @param photoByte 檔案位元組碼
     * @return 後綴（不含".")
     */
    public static String getFileExtendName(byte[] photoByte) {
        String strFileExtendName = "JPG";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
                && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97)) {
            strFileExtendName = "GIF";
        } else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70)) {
            strFileExtendName = "JPG";
        } else if ((photoByte[0] == 66) && (photoByte[1] == 77)) {
            strFileExtendName = "BMP";
        } else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71)) {
            strFileExtendName = "PNG";
        }
        return strFileExtendName;
    }
}