package com.mailist.marketing.campaign.application.port.out;

import com.mailist.marketing.campaign.domain.aggregate.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface CampaignRepository {
    Campaign save(Campaign campaign);
    Optional<Campaign> findById(Long id);
    Page<Campaign> findAll(Pageable pageable);
    Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable);
    List<Campaign> findByStatusAndScheduledAtBefore(Campaign.CampaignStatus status, java.time.LocalDateTime dateTime);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
}