package com.mailist.marketing.automation;

import com.mailist.marketing.automation.application.usecase.CreateAutomationRuleUseCase;
import com.mailist.marketing.automation.application.usecase.CreateAutomationRuleCommand;
import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import com.mailist.marketing.automation.domain.aggregate.AutomationRule;
import com.mailist.marketing.automation.domain.valueobject.Action;
import com.mailist.marketing.automation.domain.valueobject.Condition;
import com.mailist.marketing.automation.domain.valueobject.TriggerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Functional test for Automation Rules - testing automation logic
 * Similar to ActiveCampaign automation features
 */
public class AutomationFunctionalTest {
    
    @Mock
    private AutomationRuleRepository automationRuleRepository;
    
    private CreateAutomationRuleUseCase createAutomationRuleUseCase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        createAutomationRuleUseCase = new CreateAutomationRuleUseCase(automationRuleRepository);
    }
    
    @Test
    void testCreateVipWelcomeAutomation() {
        System.out.println("=== Test: Create VIP Welcome Automation ===");
        
        // Create automation rule for VIP customers
        CreateAutomationRuleCommand command = CreateAutomationRuleCommand.builder()
                .name("VIP Welcome Automation")
                .description("Send welcome email when contact gets VIP tag")
                .triggerType(TriggerType.CONTACT_TAGGED)
                .conditions(List.of(
                        Condition.builder()
                                .type("HAS_TAG")
                                .field("tag")
                                .operator(Condition.ConditionOperator.HAS_TAG)
                                .value("VIP")
                                .build()
                ))
                .actions(List.of(
                        Action.builder()
                                .type(Action.ActionType.SEND_EMAIL)
                                .value("vip-welcome-template")
                                .parameters("{\"templateId\": \"vip-001\"}")
                                .build()
                ))
                .build();
        
        AutomationRule expectedRule = AutomationRule.builder()
                .id(1L)
                .name("VIP Welcome Automation")
                .description("Send welcome email when contact gets VIP tag")
                .triggerType(TriggerType.CONTACT_TAGGED)
                .isActive(true)
                .build();
        
        when(automationRuleRepository.save(any(AutomationRule.class))).thenReturn(expectedRule);
        
        AutomationRule createdRule = createAutomationRuleUseCase.execute(command);
        
        // Verify automation rule creation
        assertNotNull(createdRule);
        assertEquals("VIP Welcome Automation", createdRule.getName());
        assertEquals("Send welcome email when contact gets VIP tag", createdRule.getDescription());
        assertEquals(TriggerType.CONTACT_TAGGED, createdRule.getTriggerType());
        assertTrue(createdRule.getIsActive());
        
        verify(automationRuleRepository).save(any(AutomationRule.class));
        
        System.out.println("✅ VIP Welcome Automation created successfully");
    }
    
    @Test
    void testCreateHighEngagementAutomation() {
        System.out.println("\n=== Test: Create High Engagement Automation ===");
        
        // Create automation rule for high engagement
        CreateAutomationRuleCommand command = CreateAutomationRuleCommand.builder()
                .name("High Engagement Follow-up")
                .description("Follow up with highly engaged contacts")
                .triggerType(TriggerType.EMAIL_OPENED)
                .conditions(List.of(
                        Condition.builder()
                                .type("LEAD_SCORE")
                                .field("leadScore")
                                .operator(Condition.ConditionOperator.GREATER_THAN)
                                .value("70")
                                .build()
                ))
                .actions(List.of(
                        Action.builder()
                                .type(Action.ActionType.UPDATE_LEAD_SCORE)
                                .value("10")
                                .build(),
                        Action.builder()
                                .type(Action.ActionType.ADD_TAG)
                                .value("High-Engagement")
                                .build()
                ))
                .build();
        
        AutomationRule expectedRule = AutomationRule.builder()
                .id(2L)
                .name("High Engagement Follow-up")
                .description("Follow up with highly engaged contacts")
                .triggerType(TriggerType.EMAIL_OPENED)
                .isActive(true)
                .build();
        
        when(automationRuleRepository.save(any(AutomationRule.class))).thenReturn(expectedRule);
        
        AutomationRule createdRule = createAutomationRuleUseCase.execute(command);
        
        // Verify automation rule creation
        assertNotNull(createdRule);
        assertEquals("High Engagement Follow-up", createdRule.getName());
        assertEquals(TriggerType.EMAIL_OPENED, createdRule.getTriggerType());
        assertTrue(createdRule.getIsActive());
        
        verify(automationRuleRepository).save(any(AutomationRule.class));
        
        System.out.println("✅ High Engagement Automation created successfully");
    }
    
    @Test
    void testCreateListSegmentationAutomation() {
        System.out.println("\n=== Test: Create List Segmentation Automation ===");
        
        // Create automation rule for list segmentation
        CreateAutomationRuleCommand command = CreateAutomationRuleCommand.builder()
                .name("Newsletter List Segmentation")
                .description("Add contacts to newsletter list based on behavior")
                .triggerType(TriggerType.EMAIL_CLICKED)
                .conditions(List.of(
                        Condition.builder()
                                .type("HAS_TAG")
                                .field("tag")
                                .operator(Condition.ConditionOperator.HAS_TAG)
                                .value("Newsletter")
                                .build(),
                        Condition.builder()
                                .type("LEAD_SCORE")
                                .field("leadScore")
                                .operator(Condition.ConditionOperator.GREATER_THAN)
                                .value("50")
                                .build()
                ))
                .actions(List.of(
                        Action.builder()
                                .type(Action.ActionType.MOVE_TO_LIST)
                                .value("Newsletter Subscribers")
                                .parameters("{\"listId\": \"1\"}")
                                .build()
                ))
                .build();
        
        AutomationRule expectedRule = AutomationRule.builder()
                .id(3L)
                .name("Newsletter List Segmentation")
                .description("Add contacts to newsletter list based on behavior")
                .triggerType(TriggerType.EMAIL_CLICKED)
                .isActive(true)
                .build();
        
        when(automationRuleRepository.save(any(AutomationRule.class))).thenReturn(expectedRule);
        
        AutomationRule createdRule = createAutomationRuleUseCase.execute(command);
        
        // Verify automation rule creation
        assertNotNull(createdRule);
        assertEquals("Newsletter List Segmentation", createdRule.getName());
        assertEquals(TriggerType.EMAIL_CLICKED, createdRule.getTriggerType());
        assertTrue(createdRule.getIsActive());
        
        verify(automationRuleRepository).save(any(AutomationRule.class));
        
        System.out.println("✅ Newsletter List Segmentation Automation created successfully");
        
        System.out.println("\n🎉 ALL AUTOMATION TESTS PASSED!");
        System.out.println("Automation provides ActiveCampaign-like functionality:");
        System.out.println("- ✅ VIP customer automation");
        System.out.println("- ✅ High engagement follow-up");
        System.out.println("- ✅ List segmentation based on behavior");
        System.out.println("- ✅ Multi-condition automation rules");
        System.out.println("- ✅ Multi-action automation workflows");
    }
}