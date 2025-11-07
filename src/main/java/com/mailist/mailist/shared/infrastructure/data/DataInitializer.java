package com.mailist.mailist.shared.infrastructure.data;

import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import com.mailist.mailist.campaign.domain.valueobject.EmailTemplate;
import com.mailist.mailist.contact.domain.valueobject.Tag;
import com.mailist.mailist.automation.domain.valueobject.Condition;
import com.mailist.mailist.automation.domain.valueobject.ConditionType;
import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.automation.application.port.out.AutomationRuleRepository;
import com.mailist.mailist.shared.application.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;
    private final CampaignRepository campaignRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final OrganizationRepository organizationRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (contactRepository.count() == 0) {
            log.info("Initializing sample data...");

            // Load default organization (created by migration 001)
            Organization defaultOrg = organizationRepository.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("Default organization not found"));

            initializeContacts(defaultOrg);
            initializeContactLists(defaultOrg);
            initializeCampaigns(defaultOrg);
            initializeAutomationRules(defaultOrg);
            log.info("Sample data initialization completed.");
        } else {
            log.info("Data already exists, skipping initialization.");
        }
    }
    
    private void initializeContacts(Organization organization) {
        log.info("Creating sample contacts...");

        Contact contact1 = Contact.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .phone("+48123456789")
                .leadScore(75)
                .tags(new HashSet<>(Arrays.asList(
                        Tag.builder().name("VIP").color("#gold").description("VIP Customer").build(),
                        Tag.builder().name("Newsletter").color("#blue").description("Newsletter Subscriber").build()
                )))
                .build();
        contact1.setTenantId(organization.getId());

        Contact contact2 = Contact.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .email("anna.nowak@example.com")
                .phone("+48987654321")
                .leadScore(45)
                .tags(new HashSet<>(Arrays.asList(
                        Tag.builder().name("Prospect").color("#green").description("Potential Customer").build(),
                        Tag.builder().name("Newsletter").color("#blue").description("Newsletter Subscriber").build()
                )))
                .build();
        contact2.setTenantId(organization.getId());

        Contact contact3 = Contact.builder()
                .firstName("Piotr")
                .lastName("Wiśniewski")
                .email("piotr.wisniewski@example.com")
                .leadScore(90)
                .tags(new HashSet<>(Arrays.asList(
                        Tag.builder().name("Customer").color("#purple").description("Active Customer").build(),
                        Tag.builder().name("High-Value").color("#red").description("High Value Customer").build()
                )))
                .build();
        contact3.setTenantId(organization.getId());

        contactRepository.save(contact1);
        contactRepository.save(contact2);
        contactRepository.save(contact3);

        log.info("Created {} sample contacts", 3);
    }
    
    private void initializeContactLists(Organization organization) {
        log.info("Creating sample contact lists...");

        ContactList newsletterList = ContactList.builder()
                .name("Newsletter Subscribers")
                .description("All newsletter subscribers")
                .isDynamic(false)
                .isActive(true)
                .build();
        newsletterList.setTenantId(organization.getId());

        ContactList vipList = ContactList.builder()
                .name("VIP Customers")
                .description("High-value VIP customers")
                .isDynamic(true)
                .isActive(true)
                .segmentRule("leadScore > 80 AND hasTag('VIP')")
                .build();
        vipList.setTenantId(organization.getId());

        contactListRepository.save(newsletterList);
        contactListRepository.save(vipList);

        log.info("Created {} sample contact lists", 2);
    }
    
    private void initializeCampaigns(Organization organization) {
        log.info("Creating sample campaigns...");

        EmailTemplate welcomeTemplate = EmailTemplate.builder()
                .templateName("Welcome Email")
                .htmlContent("<h1>Witamy w naszym serwisie!</h1><p>Dziękujemy za rejestrację.</p>")
                .textContent("Witamy w naszym serwisie! Dziękujemy za rejestrację.")
                .build();

        Campaign welcomeCampaign = Campaign.builder()
                .name("Welcome Campaign")
                .subject("Witamy w Marketing Automation!")
                .template(welcomeTemplate)
                .status(Campaign.CampaignStatus.DRAFT)
                .recipients(Set.of("jan.kowalski@example.com", "anna.nowak@example.com"))
                .build();
        welcomeCampaign.setTenantId(organization.getId());

        EmailTemplate promotionTemplate = EmailTemplate.builder()
                .templateName("Promotion Email")
                .htmlContent("<h1>Specjalna promocja!</h1><p>Skorzystaj z 20% zniżki na wszystkie produkty.</p>")
                .textContent("Specjalna promocja! Skorzystaj z 20% zniżki na wszystkie produkty.")
                .build();

        Campaign promotionCampaign = Campaign.builder()
                .name("Monthly Promotion")
                .subject("20% zniżki na wszystkie produkty!")
                .template(promotionTemplate)
                .status(Campaign.CampaignStatus.DRAFT)
                .recipients(Set.of("piotr.wisniewski@example.com"))
                .build();
        promotionCampaign.setTenantId(organization.getId());

        campaignRepository.save(welcomeCampaign);
        campaignRepository.save(promotionCampaign);

        log.info("Created {} sample campaigns", 2);
    }
    
    private void initializeAutomationRules(Organization organization) {
        log.info("Creating sample automation rules...");

        AutomationRule welcomeRule = AutomationRule.builder()
                .name("Welcome Email Automation")
                .description("Send welcome email when new contact is added")
                .triggerType(TriggerType.TAG_ADDED)
                .isActive(true)
                .build();
        welcomeRule.setTenantId(organization.getId());

        Condition newContactCondition = Condition.builder()
                .field("tag")
                .operator(Condition.ConditionOperator.HAS_TAG)
                .value("Newsletter")
                .type(ConditionType.CONTACT)
                .build();

        Action sendWelcomeEmail = Action.builder()
                .type(Action.ActionType.SEND_EMAIL)
                .value("welcome-template")
                .delayMinutes(0)
                .build();

        Action addNewCustomerTag = Action.builder()
                .type(Action.ActionType.ADD_TAG)
                .value("New Customer")
                .delayMinutes(0)
                .build();

        welcomeRule.addCondition(newContactCondition);
        welcomeRule.addAction(sendWelcomeEmail);
        welcomeRule.addElseAction(addNewCustomerTag);

        AutomationRule engagementRule = AutomationRule.builder()
                .name("High Engagement Follow-up")
                .description("Follow up with highly engaged contacts")
                .triggerType(TriggerType.EMAIL_OPENED)
                .isActive(true)
                .build();
        engagementRule.setTenantId(organization.getId());

        Condition highScoreCondition = Condition.builder()
                .field("leadScore")
                .operator(Condition.ConditionOperator.GREATER_THAN)
                .value("70")
                .type(ConditionType.CONTACT)
                .build();

        Action updateLeadScore = Action.builder()
                .type(Action.ActionType.UPDATE_LEAD_SCORE)
                .value("10")
                .delayMinutes(0)
                .build();

        Action addVipTag = Action.builder()
                .type(Action.ActionType.ADD_TAG)
                .value("VIP")
                .delayMinutes(1440) // 24 hours
                .build();

        engagementRule.addCondition(highScoreCondition);
        engagementRule.addAction(updateLeadScore);
        engagementRule.addAction(addVipTag);

        automationRuleRepository.save(welcomeRule);
        automationRuleRepository.save(engagementRule);

        log.info("Created {} sample automation rules", 2);
    }
}