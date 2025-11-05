package com.mailist.mailist.analytics.interfaces.controller;

import com.mailist.mailist.analytics.application.usecase.*;
import com.mailist.mailist.analytics.application.usecase.*;
import com.mailist.mailist.analytics.domain.aggregate.Report;
import com.mailist.mailist.analytics.interfaces.dto.ReportDto;
import com.mailist.mailist.analytics.interfaces.mapper.ReportMapper;
import com.mailist.mailist.analytics.application.port.out.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytics report management")
public class ReportController {
    
    private final GenerateReportUseCase generateReportUseCase;
    private final ExportReportUseCase exportReportUseCase;
    private final GetReportUseCase getReportUseCase;
    private final DeleteReportUseCase deleteReportUseCase;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    
    @PostMapping
    @Operation(summary = "Generate a new analytics report")
    public ResponseEntity<ReportDto.Response> generateReport(
            @Valid @RequestBody ReportDto.GenerateRequest request,
            @RequestHeader(value = "X-User-Email", defaultValue = "system") String userEmail) {
        
        GenerateReportCommand command = reportMapper.toGenerateCommand(request, userEmail);
        Report report = generateReportUseCase.execute(command);
        ReportDto.Response response = reportMapper.toResponse(report);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "List all reports with pagination")
    public ResponseEntity<Page<ReportDto.Response>> listReports(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        Page<Report> reports = reportRepository.findAll(pageable);
        Page<ReportDto.Response> response = reports.map(reportMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<ReportDto.Response> getReport(@PathVariable Long id) {
        GetReportQuery query = new GetReportQuery(id);
        Report report = getReportUseCase.execute(query);
        ReportDto.Response response = reportMapper.toResponse(report);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/export")
    @Operation(summary = "Export report in specified format")
    public ResponseEntity<ByteArrayResource> exportReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportDto.ExportRequest request,
            @RequestHeader(value = "X-User-Email", defaultValue = "system") String userEmail) {
        
        ExportReportCommand command = new ExportReportCommand(id, request.getExportFormat(), userEmail);
        ByteArrayOutputStream exportedData = exportReportUseCase.execute(command);
        
        // Get report details for filename
        GetReportQuery query = new GetReportQuery(id);
        Report report = getReportUseCase.execute(query);
        
        String filename = String.format("%s%s", 
                report.getName().replaceAll("[^a-zA-Z0-9]", "_"),
                request.getExportFormat().getFileExtension());
        
        ByteArrayResource resource = new ByteArrayResource(exportedData.toByteArray());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(request.getExportFormat().getMimeType()))
                .contentLength(exportedData.size())
                .body(resource);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete report")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", defaultValue = "system") String userEmail) {
        
        DeleteReportCommand command = new DeleteReportCommand(id, userEmail);
        deleteReportUseCase.execute(command);
        
        return ResponseEntity.noContent().build();
    }
}