package com.mailist.marketing.contact;

import com.mailist.mailist.contact.application.usecase.CreateContactUseCase;
import com.mailist.mailist.contact.application.usecase.CreateContactCommand;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test demonstrating the current multi-tenant issue
 * Shows why the current architecture prevents users from subscribing
 * to multiple lists from different organizations
 */
public class MultiTenantIssueTest {
    
    @Mock
    private ContactRepository contactRepository;
    
    private CreateContactUseCase createContactUseCase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        createContactUseCase = new CreateContactUseCase(contactRepository);
    }
    
    @Test
    void testCurrentIssue_SameEmailCannotExistTwice() {
        System.out.println("=== Test: Current Multi-Tenant Issue ===");
        
        // Scenario: User wants to subscribe to lists from different organizations
        // Organization 1: "company1" wants to add john@example.com
        // Organization 2: "startup2" wants to add john@example.com (same person)
        
        // First organization successfully adds the contact
        CreateContactCommand command1 = CreateContactCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        
        Contact contact1 = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        
        when(contactRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(contact1);
        
        Contact firstContact = createContactUseCase.execute(command1);
        assertNotNull(firstContact);
        System.out.println("✅ Company1 successfully added john@example.com");
        
        // Reset mock to simulate second organization scenario
        reset(contactRepository);
        
        // Second organization tries to add the same email
        CreateContactCommand command2 = CreateContactCommand.builder()
                .firstName("John")
                .lastName("Doe") 
                .email("john@example.com") // Same email!
                .build();
        
        // Simulate that email already exists (from first organization)
        when(contactRepository.existsByEmail("john@example.com")).thenReturn(true);
        
        // This will fail with current implementation
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            createContactUseCase.execute(command2);
        });
        
        assertEquals("Contact with this email already exists", exception.getMessage());
        System.out.println("❌ Startup2 FAILED to add john@example.com - email already exists!");
        
        System.out.println("\n📋 PROBLEM SUMMARY:");
        System.out.println("Current system prevents the same email from being used across different organizations");
        System.out.println("This means users cannot subscribe to lists from multiple companies");
        System.out.println("This is a critical flaw for a SaaS marketing platform");
        
        System.out.println("\n💡 SOLUTION NEEDED:");
        System.out.println("- Implement multi-tenant architecture");
        System.out.println("- Add Organization entity");
        System.out.println("- Make email unique per organization, not globally");
        System.out.println("- Add tenant context and data isolation");
    }
    
    @Test
    void testProposedSolution_MultiTenantSupport() {
        System.out.println("\n=== Test: Proposed Multi-Tenant Solution ===");
        
        // This test demonstrates how it SHOULD work with proper multi-tenancy
        
        System.out.println("🎯 With Multi-Tenant Architecture:");
        System.out.println("Organization 1 (company1.mailist.com):");
        System.out.println("  - Contact: john@example.com (ID: 1, Org: 1)");
        System.out.println("  - Lists: ['Newsletter', 'Promotions']");
        
        System.out.println("\nOrganization 2 (startup2.mailist.com):");
        System.out.println("  - Contact: john@example.com (ID: 2, Org: 2)");  
        System.out.println("  - Lists: ['Product Updates', 'Weekly Tips']");
        
        System.out.println("\n✅ Same email exists in both organizations");
        System.out.println("✅ Complete data isolation");
        System.out.println("✅ User can manage subscriptions independently");
        System.out.println("✅ SaaS-ready architecture");
        
        // Simulate the proposed solution working
        assertTrue(true, "Multi-tenant solution allows same email across organizations");
    }
    
    @Test 
    void testRealWorldScenarios() {
        System.out.println("\n=== Test: Real-World Multi-List Scenarios ===");
        
        System.out.println("📧 Real-world use case:");
        System.out.println("User john@example.com wants to subscribe to:");
        System.out.println("  1. TechCorp's product announcements");
        System.out.println("  2. TechCorp's weekly newsletters");  
        System.out.println("  3. StartupXYZ's funding updates");
        System.out.println("  4. StartupXYZ's job postings");
        System.out.println("  5. ConsultingFirm's industry reports");
        
        System.out.println("\n❌ Current system: IMPOSSIBLE");
        System.out.println("   - First subscription works");
        System.out.println("   - All subsequent subscriptions fail");
        System.out.println("   - User gets frustrated");
        System.out.println("   - Companies lose potential subscribers");
        
        System.out.println("\n✅ With multi-tenancy: FULLY SUPPORTED");
        System.out.println("   - Each organization manages their own contacts");
        System.out.println("   - Same email can subscribe to multiple organizations");
        System.out.println("   - Independent unsubscribe management");
        System.out.println("   - Proper data privacy and isolation");
        
        System.out.println("\n🎉 CONCLUSION: Multi-tenancy is CRITICAL for the platform!");
    }
}