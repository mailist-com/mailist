package com.mailist.mailist.campaign.interfaces.controller;

import com.mailist.mailist.campaign.application.usecase.CampaignApplicationService;
import com.mailist.mailist.campaign.application.usecase.command.CreateCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.command.SendCampaignCommand;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.campaign.interfaces.dto.CampaignDto;
import com.mailist.mailist.campaign.interfaces.mapper.CampaignMapper;
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
@RequiredArgsConstructor
@RequestMapping("/api/campaigns")
@Tag(name = "Campaigns", description = "Email campaign management")
class CampaignController {
    

    private final CampaignApplicationService campaignApplicationService;
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    
    @PostMapping
    @Operation(summary = "Create a new email campaign")
    ResponseEntity<CampaignDto.Response> createCampaign(
            @Valid @RequestBody final CampaignDto.CreateRequest request) {
        
        final CreateCampaignCommand command = campaignMapper.toCreateCommand(request);
        final Campaign campaign = campaignApplicationService.create(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignMapper.toResponse(campaign));
    }
    
    @GetMapping
    @Operation(summary = "List all campaigns with pagination")
    ResponseEntity<Page<CampaignDto.Response>> listCampaigns(
            @Parameter(description = "Pagination parameters") final Pageable pageable) {
        
        final Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        final Page<CampaignDto.Response> response = campaigns.map(campaignMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/send")
    @Operation(summary = "Send a campaign immediately")
    ResponseEntity<CampaignDto.Response> sendCampaign(
            @PathVariable final long id,
            @Valid @RequestBody final CampaignDto.SendRequest request) {
        
        final SendCampaignCommand command = campaignMapper.toSendCommand(id, request);
        final Campaign campaign = campaignApplicationService.sendCampaing(command);

        return ResponseEntity.ok(campaignMapper.toResponse(campaign));
    }
}