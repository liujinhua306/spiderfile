package com.bluedon.spiderfile.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liujh
 * @date 2019/9/19 13:36
 */
@Component
@ConfigurationProperties(prefix = "http-pool")
@Data
public class HttpPoolProperties {

    private Integer maxTotal;
    private Integer defaultMaxPerRoute;
    private Integer connectTimeout;
    private Integer connectionRequestTimeout;
    private Integer socketTimeout;
    private Integer validateAfterInactivity;

}
