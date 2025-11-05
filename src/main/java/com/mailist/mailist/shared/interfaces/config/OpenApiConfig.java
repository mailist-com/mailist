package com.mailist.mailist.shared.interfaces.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI marketingAutomationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketing Automation Platform API")
                        .description("Comprehensive marketing automation platform with email campaigns, automation rules, contact management, and analytics")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Marketing Automation Team")
                                .email("support@mailist.com")
                                .url("https://mailist.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.mailist.com")
                                .description("Production server")
                ));
    }
}