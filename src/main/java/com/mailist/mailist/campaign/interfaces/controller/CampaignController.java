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
@RequestMapping("/api/v1/campaigns")
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
    
    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID")
    ResponseEntity<CampaignDto.Response> getCampaignById(@PathVariable final long id) {
        final Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return ResponseEntity.ok(campaignMapper.toResponse(campaign));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a campaign")
    ResponseEntity<Void> deleteCampaign(@PathVariable final long id) {
        campaignRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate a campaign")
    ResponseEntity<CampaignDto.Response> duplicateCampaign(@PathVariable final long id) {
        final Campaign original = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        final Campaign duplicate = Campaign.builder()
                .name(original.getName() + " (kopia)")
                .subject(original.getSubject())
                .preheader(original.getPreheader())
                .fromName(original.getFromName())
                .fromEmail(original.getFromEmail())
                .replyTo(original.getReplyTo())
                .type(original.getType())
                .template(original.getTemplate())
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        final Campaign saved = campaignRepository.save(duplicate);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignMapper.toResponse(saved));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause a campaign")
    ResponseEntity<CampaignDto.Response> pauseCampaign(@PathVariable final long id) {
        final Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaign.pause();
        final Campaign saved = campaignRepository.save(campaign);

        return ResponseEntity.ok(campaignMapper.toResponse(saved));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume a paused campaign")
    ResponseEntity<CampaignDto.Response> resumeCampaign(@PathVariable final long id) {
        final Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaign.resume();
        final Campaign saved = campaignRepository.save(campaign);

        return ResponseEntity.ok(campaignMapper.toResponse(saved));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get campaign statistics")
    ResponseEntity<java.util.Map<String, Object>> getCampaignStatistics() {
        final long total = campaignRepository.count();
        final long draft = campaignRepository.countByStatus(Campaign.CampaignStatus.DRAFT);
        final long scheduled = campaignRepository.countByStatus(Campaign.CampaignStatus.SCHEDULED);
        final long sent = campaignRepository.countByStatus(Campaign.CampaignStatus.SENT);

        final java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("draft", draft);
        stats.put("scheduled", scheduled);
        stats.put("sent", sent);

        return ResponseEntity.ok(stats);
    }
}