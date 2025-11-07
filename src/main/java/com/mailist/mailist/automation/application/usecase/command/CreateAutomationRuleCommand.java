package com.mailist.mailist.automation.application.usecase.command;

import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.Condition;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAutomationRuleCommand {
    private String name;
    private String description;
    private TriggerType triggerType;
    private List<Condition> conditions;
    private List<Action> actions;
    private List<Action> elseActions;
}