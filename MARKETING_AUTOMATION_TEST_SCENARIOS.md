# Marketing Automation Platform - Comprehensive Test Scenarios

## Overview
This document provides comprehensive test scenarios for the marketing automation platform based on the actual implemented functionality. The platform follows a hexagonal architecture with clear separation between application, domain, and infrastructure layers.

## Implemented Features Analysis

### Core Domains Implemented:
1. **Contact Management** - Full CRUD operations, tagging
2. **Campaign Management** - Creation, sending, tracking
3. **Automation Rules** - Trigger-based automation with conditions and actions
4. **Analytics & Reporting** - Report generation and export
5. **Email Tracking** - Open and click tracking with event publishing

### Missing Implementations:
- **Contact List Management Controller** (domain exists but no REST endpoints)
- **List-to-contact associations** (no endpoints to add/remove contacts from lists)
- **Dynamic list segmentation execution** (rules defined but not executed)

---

## 1. Contact Management Flow

### Test Case 1.1: Create and Retrieve Contact
**Objective**: Test the complete contact lifecycle

**API Endpoint**: `POST /api/contacts`
**Request Payload**:
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "test@example.com",
  "phone": "+1234567890"
}
```

**Expected Response**: 
```json
{
  "id": 4,
  "firstName": "John",
  "lastName": "Doe",
  "email": "test@example.com",
  "phone": "+1234567890",
  "leadScore": 0,
  "tags": [],
  "lists": [],
  "lastActivityAt": null,
  "createdAt": "2024-11-01T10:00:00",
  "updatedAt": "2024-11-01T10:00:00"
}
```

**Verification Steps**:
1. Verify contact created with HTTP 201 status
2. Retrieve contact by ID: `GET /api/contacts/4`
3. Retrieve contact by email: `GET /api/contacts/email/test@example.com`
4. Verify all contact data matches input

**Business Logic**: 
- Contact aggregate creation
- Automatic timestamp generation
- Default lead score of 0
- Empty tags and lists collections

### Test Case 1.2: Add Tags to Contact
**Objective**: Test tag assignment and retrieval

**API Endpoint**: `POST /api/contacts/4/tags`
**Request Payload**:
```json
{
  "tagName": "VIP",
  "tagColor": "#FF5733",
  "tagDescription": "Very Important Person"
}
```

**Expected Response**:
```json
{
  "id": 4,
  "firstName": "John", 
  "lastName": "Doe",
  "email": "test@example.com",
  "tags": [
    {
      "name": "VIP",
      "color": "#FF5733", 
      "description": "Very Important Person"
    }
  ]
}
```

**Verification Steps**:
1. Add "VIP" tag with success response
2. Add "Newsletter" tag: 
   ```json
   {
     "tagName": "Newsletter",
     "tagColor": "#0066CC",
     "tagDescription": "Newsletter Subscriber"
   }
   ```
3. Retrieve contact and verify both tags are present
4. Check database: `SELECT * FROM contact_tags WHERE contact_id = 4`

**Business Logic**:
- ContactTagAddedEvent published for automation triggers
- Tag value object creation and association
- Event-driven architecture for automation workflows

---

## 2. Email Campaign Flow

### Test Case 2.1: Create Email Campaign
**Objective**: Test campaign creation with HTML template

**API Endpoint**: `POST /api/campaigns`
**Request Payload**:
```json
{
  "name": "Welcome Email Campaign",
  "subject": "Welcome to Our Platform!",
  "htmlContent": "<h1>Welcome {{firstName}}!</h1><p>Thanks for joining us.</p>",
  "textContent": "Welcome {{firstName}}! Thanks for joining us.",
  "templateName": "welcome-template",
  "recipients": ["test@example.com", "john.doe@example.com"]
}
```

**Expected Response**:
```json
{
  "id": 3,
  "name": "Welcome Email Campaign", 
  "subject": "Welcome to Our Platform!",
  "status": "DRAFT",
  "recipientCount": 2,
  "scheduledAt": null,
  "sentAt": null,
  "createdAt": "2024-11-01T10:15:00",
  "updatedAt": "2024-11-01T10:15:00"
}
```

**Verification Steps**:
1. Campaign created with DRAFT status
2. Recipient count matches input
3. List campaigns: `GET /api/campaigns`
4. Verify campaign appears in list

**Business Logic**:
- Campaign aggregate with EmailTemplate value object
- Status management (DRAFT -> SENT)
- Recipient collection management

### Test Case 2.2: Send Campaign
**Objective**: Test campaign execution and email delivery

**API Endpoint**: `POST /api/campaigns/3/send`
**Request Payload**:
```json
{
  "senderEmail": "noreply@company.com"
}
```

**Expected Response**:
```json
{
  "id": 3,
  "name": "Welcome Email Campaign",
  "status": "SENT",
  "sentAt": "2024-11-01T10:20:00",
  "recipientCount": 2
}
```

**Verification Steps**:
1. Campaign status changes to SENT
2. sentAt timestamp is populated
3. Check EmailLabs integration logs
4. Verify tracking pixels embedded in HTML content

**Business Logic**:
- EmailGateway integration (EmailLabs)
- Email template processing with tracking pixels
- Campaign status lifecycle management
- Asynchronous email sending

---

## 3. Tag-Based Automation Flow

### Test Case 3.1: Create VIP Welcome Automation
**Objective**: Test automation rule creation for tag-based triggers

**API Endpoint**: `POST /api/automation-rules`
**Request Payload**:
```json
{
  "name": "VIP Welcome Automation",
  "description": "Send welcome email when contact gets VIP tag", 
  "triggerType": "CONTACT_TAGGED",
  "isActive": true,
  "conditions": [
    {
      "type": "HAS_TAG",
      "field": "tag",
      "operator": "EQUALS", 
      "value": "VIP"
    }
  ],
  "actions": [
    {
      "type": "SEND_EMAIL",
      "target": "vip-welcome-template",
      "parameters": "{\"templateId\": \"vip-001\"}"
    }
  ]
}
```

**Expected Response**:
```json
{
  "id": 3,
  "name": "VIP Welcome Automation",
  "triggerType": "CONTACT_TAGGED", 
  "isActive": true,
  "conditions": [
    {
      "type": "HAS_TAG",
      "field": "tag", 
      "operator": "EQUALS",
      "value": "VIP"
    }
  ],
  "actions": [
    {
      "type": "SEND_EMAIL",
      "target": "vip-welcome-template",
      "parameters": "{\"templateId\": \"vip-001\"}"
    }
  ]
}
```

**Business Logic**:
- AutomationRule aggregate with Condition and Action value objects
- TriggerType enum validation
- Condition and Action collections management

### Test Case 3.2: Test Automation Execution
**Objective**: Verify automation triggers when contact is tagged

**Prerequisite**: VIP automation rule created (Test 3.1)

**Steps**:
1. Create new contact:
   ```bash
   POST /api/contacts
   {
     "firstName": "Jane",
     "lastName": "Smith", 
     "email": "jane.smith@example.com"
   }
   ```

2. Add VIP tag to trigger automation:
   ```bash
   POST /api/contacts/{contactId}/tags
   {
     "tagName": "VIP",
     "tagColor": "#FF5733"
   }
   ```

**Verification Steps**:
1. ContactTagAddedEvent published
2. AutomationEventHandler processes event
3. Check automation execution logs
4. Verify VIP welcome email queued/sent

**Business Logic**:
- Event-driven automation execution
- AutomationEngine processes rules
- EventHandlerStrategy pattern implementation
- Asynchronous action execution

---

## 4. List Management Flow (Limitations Identified)

### Test Case 4.1: Contact List Operations (Manual Testing Required)
**Issue**: No REST endpoints for list management operations

**Missing Endpoints**:
- `POST /api/contact-lists` - Create new list
- `POST /api/contact-lists/{id}/contacts` - Add contact to list  
- `DELETE /api/contact-lists/{id}/contacts/{contactId}` - Remove contact from list
- `GET /api/contact-lists` - List all contact lists

**Workaround for Testing**:
Use database directly to test domain logic:

```sql
-- Create new list
INSERT INTO contact_lists (name, description, is_dynamic, segment_rule) 
VALUES ('Newsletter Subscribers', 'Main newsletter list', false, null);

-- Add contact to list
INSERT INTO contact_list_contacts (list_id, contact_id) 
VALUES (1, 4);

-- Verify association
SELECT cl.name, c.email 
FROM contact_lists cl
JOIN contact_list_contacts clc ON cl.id = clc.list_id  
JOIN contacts c ON clc.contact_id = c.id;
```

**Domain Logic Available**:
- ContactList aggregate with contact associations
- Dynamic vs static list differentiation
- Segment rule storage (not executed)

---

## 5. Email Tracking & Analytics Flow

### Test Case 5.1: Email Open Tracking
**Objective**: Test email open event tracking

**API Endpoint**: `GET /api/tracking/open`
**Query Parameters**:
- `contactEmail=test@example.com`
- `campaignId=3`
- `messageId=msg_12345`

**Expected Response**: 
- HTTP 200 with 1x1 transparent GIF pixel
- Content-Type: image/gif

**Verification Steps**:
1. Email opened event published
2. Contact lastActivityAt updated
3. Check application logs for event processing
4. Verify automation rules triggered by EMAIL_OPENED

**Business Logic**:
- EmailOpenedEvent publication
- Contact activity tracking
- Event-driven automation triggers
- Transparent pixel tracking

### Test Case 5.2: Email Click Tracking  
**Objective**: Test click tracking with URL redirection

**API Endpoint**: `GET /api/tracking/click`
**Query Parameters**:
- `contactEmail=test@example.com`
- `campaignId=3` 
- `messageId=msg_12345`
- `url=https://company.com/landing-page`

**Expected Response**:
- HTTP 302 redirect to target URL
- Contact lead score incremented by 5

**Verification Steps**:
1. EmailClickedEvent published
2. Contact lead score increased
3. Browser redirected to target URL
4. Event processed by automation engine

**Business Logic**:
- EmailClickedEvent with URL tracking
- Lead score increment logic
- HTTP redirection handling
- Enhanced contact engagement scoring

---

## 6. Analytics & Reporting Flow

### Test Case 6.1: Generate Campaign Report
**Objective**: Test analytics report generation

**API Endpoint**: `POST /api/reports`
**Request Payload**:
```json
{
  "name": "Q4 Campaign Analytics",
  "description": "Campaign performance report for Q4 2024",
  "reportType": "CAMPAIGN", 
  "reportFormat": "PDF",
  "entityId": 3,
  "startDate": "2024-10-01T00:00:00",
  "endDate": "2024-12-31T23:59:59"
}
```

**Expected Response**:
```json
{
  "id": 1,
  "name": "Q4 Campaign Analytics",
  "reportType": "CAMPAIGN",
  "reportFormat": "PDF", 
  "data": {
    "totalSent": 1000,
    "totalDelivered": 950,
    "totalOpened": 380,
    "totalClicked": 76,
    "deliveryRate": 95.0,
    "openRate": 40.0,
    "clickRate": 20.0,
    "periodStart": "2024-10-01T00:00:00",
    "periodEnd": "2024-12-31T23:59:59"
  },
  "generatedAt": "2024-11-01T10:30:00",
  "generatedBy": "system"
}
```

**Business Logic**:
- Report aggregate with ReportData value object
- AnalyticsDataProvider integration
- ReportGenerator service execution
- Metrics calculation algorithms

### Test Case 6.2: Export Report
**Objective**: Test report export functionality

**API Endpoint**: `POST /api/reports/1/export`
**Request Payload**:
```json
{
  "exportFormat": "CSV"
}
```

**Expected Response**:
- Content-Type: text/csv
- Content-Disposition: attachment; filename="Q4_Campaign_Analytics.csv"
- Binary CSV data

**Verification Steps**:
1. Report exported successfully
2. File downloaded with correct name
3. CSV contains expected analytics data
4. Headers and formatting correct

**Business Logic**:
- ReportExporter format conversion
- File naming conventions
- MIME type handling
- Binary data streaming

---

## Identified Issues & Missing Features

### 1. Critical Missing Implementations

#### Contact List Management
**Issue**: Domain exists but no REST API
**Impact**: Cannot test list-based segmentation and campaigns
**Solution Required**: 
```java
@RestController
@RequestMapping("/api/contact-lists")
public class ContactListController {
    // Missing implementation
}
```

#### Dynamic List Segmentation
**Issue**: Segment rules stored but not executed
**Impact**: Dynamic lists cannot automatically update membership
**Solution Required**: Segmentation engine implementation

### 2. Data Consistency Issues

#### Campaign-List Association
**Issue**: Campaigns use email strings, not list IDs
**Impact**: No direct campaign-to-list relationship
**Current**: Recipients as Set<String>
**Improvement**: Support for list-based campaigns

#### Automation Event Processing
**Issue**: Events published but handler execution not verified
**Impact**: Automation may not trigger reliably
**Testing Required**: Event handler integration tests

### 3. EmailLabs Integration
**Issue**: External dependency for email sending
**Testing Challenge**: Cannot test email delivery without API keys
**Workaround**: Mock EmailGateway for testing

---

## Recommended Testing Approach

### 1. Unit Testing Priority
1. Domain aggregates (Contact, Campaign, AutomationRule)
2. Value objects (Tag, EmailTemplate, Action, Condition)
3. Use case implementations
4. Event handlers

### 2. Integration Testing Focus
1. Repository implementations
2. Event publishing and handling
3. Email gateway integration
4. Report generation workflows

### 3. End-to-End Testing Scenarios
1. Contact creation → Tagging → Automation trigger → Email sent
2. Campaign creation → Send → Tracking → Analytics
3. Report generation → Export → File download

### 4. Database Testing
1. Liquibase migrations
2. JPA relationships and constraints
3. Sample data validation
4. Performance with large datasets

---

## Conclusion

The marketing automation platform has a solid foundation with most core features implemented. The main gaps are in contact list management endpoints and dynamic segmentation execution. The event-driven architecture is well-designed for automation workflows, but requires thorough testing to ensure reliable event processing.

**Immediate Testing Priorities**:
1. Contact management and tagging flows
2. Campaign creation and sending
3. Automation rule creation and event triggering
4. Analytics report generation

**Development Priorities**:
1. Implement ContactListController
2. Build dynamic segmentation engine
3. Add comprehensive event handler tests
4. Enhance campaign-list relationships