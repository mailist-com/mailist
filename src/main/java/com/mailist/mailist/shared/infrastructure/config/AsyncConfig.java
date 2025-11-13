package com.mailist.mailist.shared.infrastructure.config;

import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async execution that preserves tenant context across threads.
 * CRITICAL FOR SECURITY: Ensures tenant isolation in async operations.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "automationExecutor")
    public Executor automationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AutomationExec-");
        executor.setTaskDecorator(new TenantAwareTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-tenant-");
        executor.setTaskDecorator(new TenantAwareTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * TaskDecorator that propagates tenant context to async threads.
     * This is CRITICAL for multi-tenant security in async operations.
     */
    private static class TenantAwareTaskDecorator implements TaskDecorator {
        @Override
        @NonNull
        public Runnable decorate(@NonNull Runnable runnable) {
            // Capture tenant context from current thread
            Long tenantId = TenantContext.isSet() ? TenantContext.getOrganizationId() : null;

            return () -> {
                try {
                    // Set tenant context in async thread
                    if (tenantId != null) {
                        TenantContext.setOrganizationId(tenantId);
                        log.debug("Propagated tenant context {} to async thread", tenantId);
                    } else {
                        log.warn("No tenant context to propagate to async thread - this may be a security issue!");
                    }
                    runnable.run();
                } finally {
                    // Clean up tenant context after execution
                    TenantContext.clear();
                }
            };
        }
    }
}