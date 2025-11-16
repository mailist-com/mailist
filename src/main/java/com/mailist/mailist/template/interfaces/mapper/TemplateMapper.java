package com.mailist.mailist.template.interfaces.mapper;

import com.mailist.mailist.template.application.command.CreateTemplateCommand;
import com.mailist.mailist.template.application.command.UpdateTemplateCommand;
import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.interfaces.dto.TemplateDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TemplateMapper {

    @Mapping(target = "content.html", source = "htmlContent")
    @Mapping(target = "content.text", source = "textContent")
    @Mapping(target = "statistics.usageCount", source = "usageCount")
    @Mapping(target = "statistics.lastUsedAt", source = "lastUsedAt")
    @Mapping(target = "statistics.campaignsCount", source = "campaignsCount")
    @Mapping(target = "statistics.avgOpenRate", source = "avgOpenRate")
    @Mapping(target = "statistics.avgClickRate", source = "avgClickRate")
    TemplateDto.Response toResponse(Template template);

    List<TemplateDto.Response> toResponseList(List<Template> templates);

    @Mapping(target = "htmlContent", source = "content.html")
    @Mapping(target = "textContent", source = "content.text")
    CreateTemplateCommand toCreateCommand(TemplateDto.CreateRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "htmlContent", source = "request.content.html")
    @Mapping(target = "textContent", source = "request.content.text")
    UpdateTemplateCommand toUpdateCommand(Long id, TemplateDto.UpdateRequest request);
}
