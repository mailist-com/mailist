package com.mailist.mailist.automation.application.usecase.command;

import lombok.Value;

@Value
public class UpdateAutomationRuleCommand {
    Long id;
    String name;
    String description;
    Boolean isActive;
    String flowJson;
}