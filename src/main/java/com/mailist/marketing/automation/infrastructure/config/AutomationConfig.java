package com.mailist.marketing.automation.infrastructure.config;

import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import com.mailist.marketing.automation.application.usecase.*;
import com.mailist.marketing.automation.domain.service.AutomationEngine;
import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.shared.domain.gateway.EmailGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AutomationConfig {
    
    @Bean
    public CreateAutomationRuleUseCase createAutomationRuleUseCase(
            AutomationRuleRepository automationRuleRepository) {
        return new CreateAutomationRuleUseCase(automationRuleRepository);
    }
    
    @Bean
    public ExecuteAutomationRuleUseCase executeAutomationRuleUseCase(
            AutomationRuleRepository automationRuleRepository,
            AutomationEngine automationEngine,
            ContactRepository contactRepository) {
        return new ExecuteAutomationRuleUseCase(automationRuleRepository, automationEngine, contactRepository);
    }
    
    @Bean
    public GetAutomationRuleUseCase getAutomationRuleUseCase(
            AutomationRuleRepository automationRuleRepository) {
        return new GetAutomationRuleUseCase(automationRuleRepository);
    }
    
    @Bean
    public UpdateAutomationRuleUseCase updateAutomationRuleUseCase(
            AutomationRuleRepository automationRuleRepository) {
        return new UpdateAutomationRuleUseCase(automationRuleRepository);
    }
    
    @Bean
    public DeleteAutomationRuleUseCase deleteAutomationRuleUseCase(
            AutomationRuleRepository automationRuleRepository) {
        return new DeleteAutomationRuleUseCase(automationRuleRepository);
    }
    
    @Bean
    public AutomationEngine automationEngine(EmailGateway emailGateway) {
        return new AutomationEngine(emailGateway);
    }
}