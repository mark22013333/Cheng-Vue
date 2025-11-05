package com.cheng.common.utils.dto;

import com.cheng.common.utils.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

import java.util.List;

/**
 * ApiResponse 類別提供HTTP回應的結構化格式，包含狀態碼、請求ID、結果及預覽資料。
 * 使用@Accessors(chain = true) 以啟用鏈式調用。
 *
 * @author Cheng
 * @since 2022/9/9 下午 02:40
 **/
@Slf4j
@Data
@Builder
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private int httpStatusCode;
    private String lineRequestId;
    private String resultData;
    private JsonNode previewData;
    /**
     * Apache Commons CSV
     */
    private List<CSVRecord> csvRecords;
    private String contentType;
    private byte[] binaryData;

    public ApiResponse preview() {
        try {
            JsonNode json = JacksonUtil.toJsonNode(resultData);
            return this.setPreviewData(json);
        } catch (Exception e) {
            log.error("===> JsonNode parse error, resultData:{}\nErrMsg:{}", resultData, e.getMessage());
            return this;
        }
    }

    public static ApiResponse empty() {
        return ApiResponse.builder().build();
    }

}
