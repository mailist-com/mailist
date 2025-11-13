package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.domain.aggregate.AutomationExecution;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;
import com.mailist.mailist.automation.domain.aggregate.AutomationStepExecution;
import com.mailist.mailist.automation.infrastructure.repository.AutomationExecutionRepository;
import com.mailist.mailist.automation.infrastructure.repository.AutomationStepExecutionRepository;
import com.mailist.mailist.automation.infrastructure.repository.AutomationStepRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.notification.application.usecase.NotificationService;
import com.mailist.mailist.notification.application.usecase.dto.CreateNotificationRequest;
import com.mailist.mailist.notification.domain.aggregate.Notification;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Serwis odpowiedzialny za wykonywanie automatyzacji.
 * Zarządza cyklem życia wykonania automatyzacji od startu do końca.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationExecutionService {

    private final AutomationExecutionRepository executionRepository;
    private final AutomationStepExecutionRepository stepExecutionRepository;
    private final AutomationStepRepository stepRepository;
    private final ContactRepository contactRepository;
    private final MarketingEmailGateway marketingEmailGateway;
    private final NotificationService notificationService;

    /**
     * Rozpoczyna wykonanie automatyzacji dla danego kontaktu.
     * Tworzy rejestr wykonania i inicjuje wszystkie kroki.
     */
    @Transactional
    public void startAutomation(AutomationRule automationRule, Long contactId, Map<String, Object> triggerContext) {
        log.info("Starting automation rule {} for contact {}", automationRule.getId(), contactId);

        // Sprawdź czy kontakt istnieje
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

        // Sprawdź czy nie ma już aktywnego wykonania dla tej automatyzacji i kontaktu
        List<AutomationExecution> activeExecutions = executionRepository.findActiveExecutions(
                automationRule.getId(), contactId);
        if (!activeExecutions.isEmpty()) {
            log.info("Automation {} already running for contact {}, skipping",
                    automationRule.getId(), contactId);
            return;
        }

        // Utwórz nowe wykonanie automatyzacji
        AutomationExecution execution = AutomationExecution.builder()
                .automationRule(automationRule)
                .contactId(contactId)
                .contactEmail(contact.getEmail())
                .status(AutomationExecution.ExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .context(new HashMap<>(triggerContext != null ? triggerContext : new HashMap<>()))
                .build();

        // Dodaj dane kontaktu do kontekstu
        execution.addToContext("contactEmail", contact.getEmail());
        execution.addToContext("contactFirstName", contact.getFirstName());
        execution.addToContext("contactLastName", contact.getLastName());
        execution.addToContext("contactId", contactId);

        execution = executionRepository.save(execution);
        log.info("Created automation execution {}", execution.getId());

        // Pobierz wszystkie kroki automatyzacji
        List<AutomationStep> steps = stepRepository.findByAutomationRuleIdOrderByStepOrderAsc(
                automationRule.getId());

        if (steps.isEmpty()) {
            log.warn("No steps found for automation rule {}", automationRule.getId());
            execution.fail("Brak kroków do wykonania");
            executionRepository.save(execution);
            return;
        }

        // Utwórz rejestr wykonania dla każdego kroku
        for (AutomationStep step : steps) {
            AutomationStepExecution stepExecution = AutomationStepExecution.builder()
                    .automationExecution(execution)
                    .automationStep(step)
                    .stepId(step.getStepId())
                    .stepType(step.getStepType())
                    .status(AutomationStepExecution.StepExecutionStatus.PENDING)
                    .inputData(new HashMap<>())
                    .outputData(new HashMap<>())
                    .retryCount(0)
                    .build();
            stepExecutionRepository.save(stepExecution);
        }

        log.info("Created {} step executions for automation execution {}", steps.size(), execution.getId());

        // Utwórz notyfikację
        try {
            notificationService.createNotification(
                    CreateNotificationRequest.builder()
                            .type(Notification.NotificationType.INFO)
                            .category(Notification.NotificationCategory.AUTOMATION_TRIGGERED)
                            .title("Automatyzacja uruchomiona")
                            .message("Automatyzacja <b>" + automationRule.getName() + "</b> została uruchomiona dla kontaktu <b>" + contact.getEmail() + "</b>")
                            .actionUrl("/automation/" + automationRule.getId() + "/executions/" + execution.getId())
                            .build());
        } catch (Exception e) {
            log.error("Failed to create notification", e);
        }

        // Rozpocznij wykonywanie kroków
        processNextStep(execution.getId());
    }

    /**
     * Przetwarza następny krok w wykonaniu automatyzacji.
     */
    @Transactional
    public void processNextStep(Long executionId) {
        AutomationExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        if (!execution.isActive()) {
            log.info("Execution {} is not active, stopping", executionId);
            return;
        }

        // Znajdź następny oczekujący krok
        Optional<AutomationStepExecution> nextStepOpt = stepExecutionRepository.findNextPendingStep(executionId);

        if (nextStepOpt.isEmpty()) {
            // Brak kolejnych kroków - sprawdź czy wszystkie zakończone
            boolean allCompleted = stepExecutionRepository.areAllStepsCompleted(executionId);
            if (allCompleted) {
                execution.complete();
                executionRepository.save(execution);
                log.info("Automation execution {} completed successfully", executionId);

                // Utwórz notyfikację o zakończeniu
                try {
                    notificationService.createNotification(
                            CreateNotificationRequest.builder()
                                    .type(Notification.NotificationType.SUCCESS)
                                    .category(Notification.NotificationCategory.AUTOMATION_TRIGGERED)
                                    .title("Automatyzacja zakończona")
                                    .message("Automatyzacja <b>" + execution.getAutomationRule().getName() +
                                            "</b> zakończona dla kontaktu <b>" + execution.getContactEmail() + "</b>")
                                    .actionUrl("/automation/" + execution.getAutomationRule().getId() +
                                            "/executions/" + execution.getId())
                                    .build());
                } catch (Exception e) {
                    log.error("Failed to create notification", e);
                }
            }
            return;
        }

        AutomationStepExecution stepExecution = nextStepOpt.get();
        executeStep(stepExecution);
    }

    /**
     * Wykonuje pojedynczy krok automatyzacji.
     */
    @Transactional
    public void executeStep(AutomationStepExecution stepExecution) {
        log.info("Executing step {} of type {} for execution {}",
                stepExecution.getStepId(),
                stepExecution.getStepType(),
                stepExecution.getAutomationExecution().getId());

        stepExecution.start();
        stepExecutionRepository.save(stepExecution);

        AutomationExecution execution = stepExecution.getAutomationExecution();
        execution.updateCurrentStep(stepExecution.getStepId());
        executionRepository.save(execution);

        try {
            AutomationStep step = stepExecution.getAutomationStep();
            Map<String, Object> settings = step.getSettings();
            Map<String, Object> outputData = new HashMap<>();

            // Wykonaj krok na podstawie typu
            switch (stepExecution.getStepType().toUpperCase()) {
                case "TRIGGER":
                    // Trigger jest tylko punktem startowym, od razu sukces
                    outputData.put("triggered", true);
                    break;

                case "SEND_EMAIL":
                    executeSendEmail(stepExecution, settings, execution);
                    outputData.put("emailSent", true);
                    break;

                case "ADD_TAG":
                    executeAddTag(stepExecution, settings, execution);
                    outputData.put("tagAdded", true);
                    break;

                case "REMOVE_TAG":
                    executeRemoveTag(stepExecution, settings, execution);
                    outputData.put("tagRemoved", true);
                    break;

                case "WAIT":
                    executeWait(stepExecution, settings, execution);
                    return; // WAIT nie kończy się od razu

                case "UPDATE_LEAD_SCORE":
                    executeUpdateLeadScore(stepExecution, settings, execution);
                    outputData.put("leadScoreUpdated", true);
                    break;

                case "CONDITION":
                    boolean conditionMet = evaluateCondition(stepExecution, settings, execution);
                    outputData.put("conditionMet", conditionMet);
                    break;

                default:
                    log.warn("Unknown step type: {}", stepExecution.getStepType());
                    stepExecution.skip("Nieznany typ kroku: " + stepExecution.getStepType());
                    stepExecutionRepository.save(stepExecution);
                    processNextStep(execution.getId());
                    return;
            }

            // Krok zakończony pomyślnie
            stepExecution.complete(outputData);
            stepExecutionRepository.save(stepExecution);

            log.info("Step {} completed successfully", stepExecution.getStepId());

            // Przejdź do następnego kroku
            processNextStep(execution.getId());

        } catch (Exception e) {
            log.error("Failed to execute step {}: {}", stepExecution.getStepId(), e.getMessage(), e);

            // Sprawdź czy można retry
            if (stepExecution.canRetry(3)) {
                stepExecution.incrementRetry();
                stepExecution.fail("Błąd wykonania: " + e.getMessage() + " (próba " + stepExecution.getRetryCount() + ")");
                stepExecutionRepository.save(stepExecution);

                // Spróbuj ponownie
                executeStep(stepExecution);
            } else {
                stepExecution.fail("Błąd wykonania (przekroczono limit prób): " + e.getMessage());
                stepExecutionRepository.save(stepExecution);

                // Zakończ całe wykonanie jako nieudane
                execution.fail("Krok " + stepExecution.getStepId() + " zakończył się błędem: " + e.getMessage());
                executionRepository.save(execution);
            }
        }
    }

    private void executeSendEmail(AutomationStepExecution stepExecution,
                                   Map<String, Object> settings,
                                   AutomationExecution execution) {
        String subject = (String) settings.get("subject");
        String content = (String) settings.get("content");
        String htmlContent = (String) settings.get("htmlContent");

        if (subject == null || (content == null && htmlContent == null)) {
            throw new IllegalArgumentException("Email requires subject and content");
        }

        Contact contact = contactRepository.findById(execution.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        // Zamień placeholdery w treści
        String finalContent = replacePlaceholders(content != null ? content : htmlContent, execution);
        String finalSubject = replacePlaceholders(subject, execution);

        MarketingEmailMessage emailMessage = MarketingEmailMessage.builder()
                .to(contact.getEmail())
                .subject(finalSubject)
                .htmlContent(finalContent)
                .textContent(finalContent)
                .contactId(contact.getId().toString())
                .trackingId(UUID.randomUUID().toString())
                .build();

        marketingEmailGateway.sendEmail(emailMessage);
        log.info("Automation email sent to {}", contact.getEmail());
    }

    private void executeAddTag(AutomationStepExecution stepExecution,
                                Map<String, Object> settings,
                                AutomationExecution execution) {
        String tagName = (String) settings.get("tagName");
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name is required");
        }

        Contact contact = contactRepository.findById(execution.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        contact.addTag(tagName, null, null);
        contactRepository.save(contact);
        log.info("Added tag {} to contact {}", tagName, contact.getEmail());
    }

    private void executeRemoveTag(AutomationStepExecution stepExecution,
                                   Map<String, Object> settings,
                                   AutomationExecution execution) {
        String tagName = (String) settings.get("tagName");
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name is required");
        }

        Contact contact = contactRepository.findById(execution.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        contact.removeTag(tagName);
        contactRepository.save(contact);
        log.info("Removed tag {} from contact {}", tagName, contact.getEmail());
    }

    private void executeWait(AutomationStepExecution stepExecution,
                             Map<String, Object> settings,
                             AutomationExecution execution) {
        Object delayObj = settings.get("delay");
        Object unitObj = settings.get("unit");

        if (delayObj == null) {
            throw new IllegalArgumentException("Delay is required for WAIT step");
        }

        int delay = delayObj instanceof Integer ? (Integer) delayObj : Integer.parseInt(delayObj.toString());
        String unit = unitObj != null ? unitObj.toString() : "MINUTES";

        // Oblicz czas zaplanowanego wykonania
        LocalDateTime scheduledFor = LocalDateTime.now();
        switch (unit.toUpperCase()) {
            case "MINUTES":
                scheduledFor = scheduledFor.plusMinutes(delay);
                break;
            case "HOURS":
                scheduledFor = scheduledFor.plusHours(delay);
                break;
            case "DAYS":
                scheduledFor = scheduledFor.plusDays(delay);
                break;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + unit);
        }

        stepExecution.schedule(scheduledFor);
        stepExecutionRepository.save(stepExecution);

        execution.waitForDelay();
        executionRepository.save(execution);

        log.info("Step {} scheduled for execution at {}", stepExecution.getStepId(), scheduledFor);
    }

    private void executeUpdateLeadScore(AutomationStepExecution stepExecution,
                                        Map<String, Object> settings,
                                        AutomationExecution execution) {
        Object pointsObj = settings.get("points");
        if (pointsObj == null) {
            throw new IllegalArgumentException("Points value is required");
        }

        int points = pointsObj instanceof Integer ? (Integer) pointsObj : Integer.parseInt(pointsObj.toString());

        Contact contact = contactRepository.findById(execution.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        contact.incrementLeadScore(points);
        contactRepository.save(contact);
        log.info("Updated lead score for contact {} by {} points", contact.getEmail(), points);
    }

    private boolean evaluateCondition(AutomationStepExecution stepExecution,
                                      Map<String, Object> settings,
                                      AutomationExecution execution) {
        // TODO: Implement condition evaluation logic
        // For now, always return true
        log.warn("Condition evaluation not yet fully implemented");
        return true;
    }

    private String replacePlaceholders(String text, AutomationExecution execution) {
        if (text == null) return null;

        Map<String, Object> context = execution.getContext();
        String result = text;

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Wznawia wykonanie kroków WAIT, które są zaplanowane na teraz lub wcześniej.
     * Ta metoda powinna być wywoływana przez scheduled task.
     */
    @Transactional
    public void resumeScheduledSteps() {
        List<AutomationStepExecution> scheduledSteps = stepExecutionRepository
                .findScheduledStepsReadyToExecute(LocalDateTime.now());

        log.info("Found {} scheduled steps ready to execute", scheduledSteps.size());

        for (AutomationStepExecution stepExecution : scheduledSteps) {
            try {
                // Zmień status z SCHEDULED na COMPLETED
                stepExecution.complete(Map.of("waited", true));
                stepExecutionRepository.save(stepExecution);

                // Wznów wykonanie automatyzacji
                AutomationExecution execution = stepExecution.getAutomationExecution();
                execution.resume();
                executionRepository.save(execution);

                // Przejdź do następnego kroku
                processNextStep(execution.getId());
            } catch (Exception e) {
                log.error("Failed to resume scheduled step {}: {}",
                        stepExecution.getId(), e.getMessage(), e);
            }
        }
    }
}
