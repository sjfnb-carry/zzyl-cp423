package com.zzyl.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "baidu")
public class BaiduAIProperties {
    private String accessKey;
    private String secretKey;
    private String qianfanModel;
    private Double temperature;
    private Integer maxOutputTokens;
    private String responseFormat;
}
