package com.mailist.mailist.campaign.infrastructure.repository;

import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignJpaRepository extends JpaRepository<Campaign, Long>, CampaignRepository {

    @Override
    Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable);

    @Override
    List<Campaign> findByStatusAndScheduledAtBefore(Campaign.CampaignStatus status, LocalDateTime dateTime);
}