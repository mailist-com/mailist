package com.mailist.mailist.apikey.infrastructure.repository;

import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for ApiKeyActivity.
 */
@Repository
public interface ApiKeyActivityRepository extends JpaRepository<ApiKeyActivity, String> {

    Page<ApiKeyActivity> findByApiKeyIdOrderByTimestampDesc(Long apiKeyId, Pageable pageable);

    List<ApiKeyActivity> findByApiKeyIdAndTimestampBetweenOrderByTimestampDesc(
            Long apiKeyId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    long countByApiKeyIdAndTimestampAfter(Long apiKeyId, LocalDateTime after);

    @Modifying
    @Query("DELETE FROM ApiKeyActivity a WHERE a.apiKeyId = :apiKeyId")
    void deleteByApiKeyId(@Param("apiKeyId") Long apiKeyId);

    @Modifying
    @Query("DELETE FROM ApiKeyActivity a WHERE a.timestamp < :timestamp")
    void deleteByTimestampBefore(@Param("timestamp") LocalDateTime timestamp);

    Page<ApiKeyActivity> findByApiKeyId(Long apiKeyId, Pageable pageable);
}
