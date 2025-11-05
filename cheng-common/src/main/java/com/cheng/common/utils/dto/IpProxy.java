package com.cheng.common.utils.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author cheng
 * @since 2023/1/10 4:27 PM
 **/
@Data
@Builder
public class IpProxy {
    private String ip;
    private Integer port;
}