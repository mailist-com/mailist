package com.mailist.mailist.campaign.infrastructure.repository;

import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CampaignRepositoryImpl implements CampaignRepository {
    
    private final CampaignJpaRepository jpaRepository;
    
    @Override
    public Campaign save(Campaign campaign) {
        return jpaRepository.save(campaign);
    }
    
    @Override
    public Optional<Campaign> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Page<Campaign> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status, pageable);
    }
    
    @Override
    public List<Campaign> findByStatusAndScheduledAtBefore(Campaign.CampaignStatus status, LocalDateTime dateTime) {
        return jpaRepository.findByStatusAndScheduledAtBefore(status, dateTime);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
}