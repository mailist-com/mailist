package com.mailist.mailist.automation.interfaces.mapper;

import com.mailist.mailist.automation.application.usecase.command.CreateAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.Condition;
import com.mailist.mailist.automation.interfaces.dto.AutomationRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AutomationRuleMapper {
    
    @Mapping(source = "conditions", target = "conditions")
    @Mapping(source = "actions", target = "actions")
    AutomationRuleDto.Response toResponse(AutomationRule automationRule);
    
    List<AutomationRuleDto.Response> toResponseList(List<AutomationRule> automationRules);
    
    CreateAutomationRuleCommand toCreateCommand(AutomationRuleDto.CreateRequest request);
    
    UpdateAutomationRuleCommand toUpdateCommand(Long id, AutomationRuleDto.UpdateRequest request);
    
    void updateEntityFromCommand(UpdateAutomationRuleCommand command, @MappingTarget AutomationRule automationRule);
    
    AutomationRuleDto.ConditionDto.Response toConditionResponse(Condition condition);
    
    List<AutomationRuleDto.ConditionDto.Response> toConditionResponseList(List<Condition> conditions);
    
    Condition toCondition(AutomationRuleDto.ConditionDto.CreateRequest request);
    
    List<Condition> toConditionList(List<AutomationRuleDto.ConditionDto.CreateRequest> requests);
    
    Condition toConditionFromUpdate(AutomationRuleDto.ConditionDto.UpdateRequest request);
    
    List<Condition> toConditionListFromUpdate(List<AutomationRuleDto.ConditionDto.UpdateRequest> requests);
    
    @Mapping(source = "value", target = "target")
    AutomationRuleDto.ActionDto.Response toActionResponse(Action action);
    
    List<AutomationRuleDto.ActionDto.Response> toActionResponseList(List<Action> actions);
    
    @Mapping(source = "target", target = "value")
    @Mapping(target = "delayMinutes", ignore = true)
    Action toAction(AutomationRuleDto.ActionDto.CreateRequest request);
    
    List<Action> toActionList(List<AutomationRuleDto.ActionDto.CreateRequest> requests);
    
    @Mapping(source = "target", target = "value")
    @Mapping(target = "delayMinutes", ignore = true)
    Action toActionFromUpdate(AutomationRuleDto.ActionDto.UpdateRequest request);
    
    List<Action> toActionListFromUpdate(List<AutomationRuleDto.ActionDto.UpdateRequest> requests);
}