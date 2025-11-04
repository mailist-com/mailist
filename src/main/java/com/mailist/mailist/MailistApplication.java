package com.mailist.mailist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import com.mailist.marketing.shared.infrastructure.config.JpaConfig;
import com.mailist.marketing.campaign.infrastructure.config.CampaignConfig;
import com.mailist.marketing.contact.infrastructure.config.ContactConfig;
import com.mailist.marketing.automation.infrastructure.config.AutomationConfig;
import com.mailist.marketing.analytics.infrastructure.config.AnalyticsConfig;

@SpringBootApplication(scanBasePackages = "com.mailist.marketing")
@EnableConfigurationProperties
@EnableAsync
@Import({
    JpaConfig.class,
    CampaignConfig.class,
    ContactConfig.class,
    AutomationConfig.class,
    AnalyticsConfig.class
})
public class MailistApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailistApplication.class, args);
	}

}
