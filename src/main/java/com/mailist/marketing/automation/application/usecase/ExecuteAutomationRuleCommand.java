package com.mailist.marketing.automation.application.usecase;

import lombok.Value;

@Value
public class ExecuteAutomationRuleCommand {
    Long automationRuleId;
    Long contactId;
}