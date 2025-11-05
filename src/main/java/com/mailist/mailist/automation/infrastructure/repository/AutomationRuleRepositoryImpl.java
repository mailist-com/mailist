package com.mailist.mailist.automation.infrastructure.repository;

import com.mailist.mailist.automation.application.port.out.AutomationRuleRepository;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AutomationRuleRepositoryImpl implements AutomationRuleRepository {
    
    private final AutomationRuleJpaRepository jpaRepository;
    
    @Override
    public AutomationRule save(AutomationRule automationRule) {
        return jpaRepository.save(automationRule);
    }
    
    @Override
    public Optional<AutomationRule> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Page<AutomationRule> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public List<AutomationRule> findByIsActiveTrue() {
        return jpaRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<AutomationRule> findByTriggerType(TriggerType triggerType) {
        return jpaRepository.findByTriggerType(triggerType);
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
    public long countActive() {
        return jpaRepository.countActive();
    }
}