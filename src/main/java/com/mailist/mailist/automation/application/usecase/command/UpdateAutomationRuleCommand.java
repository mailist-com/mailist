package com.mailist.mailist.automation.application.usecase.command;

import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.Condition;
import lombok.Value;

import java.util.List;

@Value
public class UpdateAutomationRuleCommand {
    Long id;
    String name;
    String description;
    Boolean isActive;
    List<Condition> conditions;
    List<Action> actions;
}