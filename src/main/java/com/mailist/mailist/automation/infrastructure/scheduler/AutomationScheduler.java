package com.mailist.mailist.automation.infrastructure.scheduler;

import com.mailist.mailist.automation.application.usecase.AutomationExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler dla automatyzacji.
 * Odpowiedzialny za wznowienie zaplanowanych kroków WAIT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationScheduler {

    private final AutomationExecutionService automationExecutionService;

    /**
     * Sprawdza co minutę czy są zaplanowane kroki do wykonania.
     */
    @Scheduled(fixedDelay = 60000) // Co 60 sekund
    public void processScheduledSteps() {
        try {
            log.debug("Checking for scheduled automation steps...");
            automationExecutionService.resumeScheduledSteps();
        } catch (Exception e) {
            log.error("Error processing scheduled automation steps", e);
        }
    }
}
