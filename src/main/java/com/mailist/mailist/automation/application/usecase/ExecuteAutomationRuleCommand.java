package com.mailist.mailist.automation.application.usecase;

import lombok.Value;

@Value
public class ExecuteAutomationRuleCommand {
    Long automationRuleId;
    Long contactId;
}