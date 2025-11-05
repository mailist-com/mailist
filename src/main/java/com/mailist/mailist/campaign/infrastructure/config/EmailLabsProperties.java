package com.mailist.mailist.campaign.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "emaillabs")
public class EmailLabsProperties {
    private String apiBaseUrl = "https://api.emaillabs.net.pl/v1";
    private String apiKey;
    private String secret;
    private int timeoutSeconds = 30;
    private int retryAttempts = 3;
}