package com.mailist.mailist.apikey.application.port.out;

import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository port for ApiKeyActivity.
 */
public interface ApiKeyActivityRepository {

    ApiKeyActivity save(ApiKeyActivity activity);

    Page<ApiKeyActivity> findByApiKeyId(Long apiKeyId, Pageable pageable);

    List<ApiKeyActivity> findByApiKeyIdAndTimestampBetween(
            Long apiKeyId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    long countByApiKeyIdAndTimestampAfter(Long apiKeyId, LocalDateTime after);

    void deleteByApiKeyId(Long apiKeyId);

    void deleteOlderThan(LocalDateTime timestamp);
}
