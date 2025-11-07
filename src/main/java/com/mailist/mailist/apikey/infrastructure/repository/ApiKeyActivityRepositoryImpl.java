package com.mailist.mailist.apikey.infrastructure.repository;

import com.mailist.mailist.apikey.application.port.out.ApiKeyActivityRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ApiKeyActivityRepository using JPA.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyActivityRepositoryImpl implements ApiKeyActivityRepository {

    private final ApiKeyActivityJpaRepository jpaRepository;

    @Override
    public ApiKeyActivity save(ApiKeyActivity activity) {
        return jpaRepository.save(activity);
    }

    @Override
    public Page<ApiKeyActivity> findByApiKeyId(Long apiKeyId, Pageable pageable) {
        return jpaRepository.findByApiKeyIdOrderByTimestampDesc(apiKeyId, pageable);
    }

    @Override
    public List<ApiKeyActivity> findByApiKeyIdAndTimestampBetween(
            Long apiKeyId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return jpaRepository.findByApiKeyIdAndTimestampBetweenOrderByTimestampDesc(
                apiKeyId, startTime, endTime
        );
    }

    @Override
    public long countByApiKeyIdAndTimestampAfter(Long apiKeyId, LocalDateTime after) {
        return jpaRepository.countByApiKeyIdAndTimestampAfter(apiKeyId, after);
    }

    @Override
    @Transactional
    public void deleteByApiKeyId(Long apiKeyId) {
        jpaRepository.deleteByApiKeyId(apiKeyId);
    }

    @Override
    @Transactional
    public void deleteOlderThan(LocalDateTime timestamp) {
        jpaRepository.deleteByTimestampBefore(timestamp);
    }
}
