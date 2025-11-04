package com.mailist.marketing.campaign.infrastructure.repository;

import com.mailist.marketing.campaign.domain.aggregate.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CampaignJpaRepository extends JpaRepository<Campaign, Long> {
    Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable);
    List<Campaign> findByStatusAndScheduledAtBefore(Campaign.CampaignStatus status, LocalDateTime dateTime);
}