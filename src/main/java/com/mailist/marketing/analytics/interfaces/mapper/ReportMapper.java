package com.mailist.marketing.analytics.interfaces.mapper;

import com.mailist.marketing.analytics.application.usecase.GenerateReportCommand;
import com.mailist.marketing.analytics.domain.aggregate.Report;
import com.mailist.marketing.analytics.domain.valueobject.ReportData;
import com.mailist.marketing.analytics.interfaces.dto.ReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {
    
    @Mapping(target = "data", ignore = true)
    @Mapping(target = "isExpired", expression = "java(false)")
    @Mapping(target = "hasFile", expression = "java(false)")
    ReportDto.Response toResponse(Report report);
    
    List<ReportDto.Response> toResponseList(List<Report> reports);
    
    GenerateReportCommand toGenerateCommand(ReportDto.GenerateRequest request, String generatedBy);
}