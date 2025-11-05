package com.mailist.mailist.campaign.interfaces.controller;

import com.mailist.mailist.campaign.application.usecase.CreateCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.CreateCampaignUseCase;
import com.mailist.mailist.campaign.application.usecase.SendCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.SendCampaignUseCase;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.interfaces.dto.CampaignDto;
import com.mailist.mailist.campaign.interfaces.mapper.CampaignMapper;
import com.mailist.mailist.campaign.application.usecase.*;
import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Email campaign management")
public class CampaignController {
    
    private final CreateCampaignUseCase createCampaignUseCase;
    private final SendCampaignUseCase sendCampaignUseCase;
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    
    @PostMapping
    @Operation(summary = "Create a new email campaign")
    public ResponseEntity<CampaignDto.Response> createCampaign(
            @Valid @RequestBody CampaignDto.CreateRequest request) {
        
        CreateCampaignCommand command = campaignMapper.toCreateCommand(request);
        Campaign campaign = createCampaignUseCase.execute(command);
        CampaignDto.Response response = campaignMapper.toResponse(campaign);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "List all campaigns with pagination")
    public ResponseEntity<Page<CampaignDto.Response>> listCampaigns(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        Page<CampaignDto.Response> response = campaigns.map(campaignMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/send")
    @Operation(summary = "Send a campaign immediately")
    public ResponseEntity<CampaignDto.Response> sendCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignDto.SendRequest request) {
        
        SendCampaignCommand command = campaignMapper.toSendCommand(id, request);
        Campaign campaign = sendCampaignUseCase.execute(command);
        CampaignDto.Response response = campaignMapper.toResponse(campaign);
        
        return ResponseEntity.ok(response);
    }
}