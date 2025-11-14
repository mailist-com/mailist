package com.mailist.mailist.campaign.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.ses")
public class AwsSesProperties {
    private String region = "eu-west-1";
    private String accessKeyId;
    private String secretAccessKey;
    private String configurationSetName;
    private int timeoutSeconds = 30;
}
