package com.mailist.mailist.automation.interfaces.mapper;

import com.mailist.mailist.automation.application.usecase.command.CreateAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.interfaces.dto.AutomationRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AutomationRuleMapper {

    AutomationRuleDto.Response toResponse(AutomationRule automationRule);

    List<AutomationRuleDto.Response> toResponseList(List<AutomationRule> automationRules);

    CreateAutomationRuleCommand toCreateCommand(AutomationRuleDto.CreateRequest request);

    UpdateAutomationRuleCommand toUpdateCommand(Long id, AutomationRuleDto.UpdateRequest request);

    void updateEntityFromCommand(UpdateAutomationRuleCommand command, @MappingTarget AutomationRule automationRule);
}