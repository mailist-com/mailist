package com.mailist.mailist.analytics.interfaces.controller;

import com.mailist.mailist.analytics.application.service.DashboardService;
import com.mailist.mailist.analytics.interfaces.dto.DashboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and overview")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview-stats")
    @Operation(summary = "Get dashboard overview statistics")
    public ResponseEntity<DashboardDto.OverviewStats> getOverviewStats() {
        DashboardDto.OverviewStats stats = dashboardService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-campaigns")
    @Operation(summary = "Get recent campaigns with statistics")
    public ResponseEntity<DashboardDto.RecentCampaigns> getRecentCampaigns(
            @Parameter(description = "Number of campaigns to return")
            @RequestParam(defaultValue = "5") int limit) {
        DashboardDto.RecentCampaigns campaigns = dashboardService.getRecentCampaigns(limit);
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/growth-data")
    @Operation(summary = "Get growth data for contacts and emails by month")
    public ResponseEntity<DashboardDto.GrowthData> getGrowthData(
            @Parameter(description = "Year for growth data")
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDateTime.now().getYear();
        DashboardDto.GrowthData growthData = dashboardService.getGrowthData(targetYear);
        return ResponseEntity.ok(growthData);
    }

    @GetMapping("/activity-feed")
    @Operation(summary = "Get recent activity feed")
    public ResponseEntity<DashboardDto.ActivityFeed> getActivityFeed(
            @Parameter(description = "Number of activities to return")
            @RequestParam(defaultValue = "10") int limit) {
        DashboardDto.ActivityFeed activityFeed = dashboardService.getActivityFeed(limit);
        return ResponseEntity.ok(activityFeed);
    }

    @GetMapping("/usage-statistics")
    @Operation(summary = "Get usage statistics for current organization")
    public ResponseEntity<DashboardDto.UsageStatistics> getUsageStatistics() {
        DashboardDto.UsageStatistics usageStats = dashboardService.getUsageStatistics();
        return ResponseEntity.ok(usageStats);
    }

    @GetMapping("/usage-alerts")
    @Operation(summary = "Get usage alerts for current organization")
    public ResponseEntity<DashboardDto.UsageAlerts> getUsageAlerts() {
        DashboardDto.UsageAlerts usageAlerts = dashboardService.getUsageAlerts();
        return ResponseEntity.ok(usageAlerts);
    }
}
