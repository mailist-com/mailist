/**
 * BACKEND MODELS - TypeScript Interfaces
 *
 * Ten plik zawiera wszystkie modele danych, które powinny być zaimplementowane
 * po stronie backendu. Możesz użyć tych interfejsów jako referencję dla
 * struktury bazy danych i walidacji API.
 *
 * Uwaga: Adaptuj te modele do swojego stack'u (Node.js, Python, Java, etc.)
 */

// ============================================================================
// AUTHENTICATION & USER MODELS
// ============================================================================

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string; // Hashed! Nigdy nie przesyłaj plaintext
  role: 'admin' | 'user' | 'owner';
  avatar?: string;
  phone?: string;
  company?: string;
  timezone: string;
  language: string;
  emailVerified: boolean;
  twoFactorEnabled: boolean;
  preferences: UserPreferences;
  createdAt: Date;
  updatedAt: Date;
  lastLoginAt?: Date;
}

export interface UserPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  weeklyReport: boolean;
  marketingEmails: boolean;
  darkMode: boolean;
}

export interface AuthToken {
  userId: string;
  token: string; // JWT token
  refreshToken: string;
  expiresAt: Date;
  createdAt: Date;
}

export interface TeamMember {
  id: string;
  userId: string;
  organizationId: string;
  role: 'owner' | 'admin' | 'member' | 'viewer';
  permissions: string[];
  status: 'active' | 'pending' | 'suspended';
  invitedBy: string;
  invitedAt: Date;
  joinedAt?: Date;
  lastActiveAt?: Date;
}

// ============================================================================
// CONTACT MODELS
// ============================================================================

export interface Contact {
  id: string;
  organizationId: string; // Dla multi-tenancy
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  organization?: string;
  tags: string[];
  customFields: CustomField[];
  lists: string[]; // Array of list IDs
  status: 'active' | 'inactive' | 'bounced' | 'unsubscribed';
  subscriptionStatus: 'subscribed' | 'unsubscribed' | 'pending';
  source: 'manual' | 'import' | 'api' | 'form' | 'integration';
  ipAddress?: string;
  createdAt: Date;
  updatedAt: Date;
  lastActivity?: Date;
  engagementScore: number; // 0-100
  location?: ContactLocation;
}

export interface ContactLocation {
  country?: string;
  countryCode?: string;
  state?: string;
  city?: string;
  postalCode?: string;
  timezone?: string;
  latitude?: number;
  longitude?: number;
}

export interface CustomField {
  id: string;
  name: string;
  value: any;
  type: 'text' | 'number' | 'date' | 'dropdown' | 'checkbox' | 'url' | 'email';
}

export interface ContactFilter {
  search?: string;
  status?: ('active' | 'inactive' | 'bounced' | 'unsubscribed')[];
  subscriptionStatus?: ('subscribed' | 'unsubscribed' | 'pending')[];
  lists?: string[];
  tags?: string[];
  dateFrom?: Date;
  dateTo?: Date;
  engagementScoreMin?: number;
  engagementScoreMax?: number;
}

export interface ContactSegment {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  conditions: SegmentCondition[];
  contactCount: number; // Cache count, update periodically
  isAutoUpdate: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface SegmentCondition {
  field: string; // e.g., 'tags', 'status', 'engagementScore', 'customFields.jobTitle'
  operator: 'equals' | 'not_equals' | 'contains' | 'not_contains' | 'starts_with' | 'ends_with' | 'greater' | 'less' | 'greater_or_equal' | 'less_or_equal' | 'is_empty' | 'is_not_empty';
  value: any;
}

// ============================================================================
// LIST MODELS
// ============================================================================

export interface ContactList {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  type: 'standard' | 'smart';
  subscriberCount: number;
  activeSubscriberCount: number;
  isSmartList: boolean;
  smartListConditions?: SegmentCondition[];
  isArchived: boolean;
  source: 'manual' | 'import' | 'api' | 'automation';
  createdAt: Date;
  updatedAt: Date;
}

export interface ListSubscription {
  id: string;
  listId: string;
  contactId: string;
  status: 'subscribed' | 'unsubscribed' | 'pending';
  subscribedAt: Date;
  unsubscribedAt?: Date;
  source: 'manual' | 'import' | 'form' | 'api';
  ipAddress?: string;
  doubleOptInConfirmed: boolean;
}

export interface ListImport {
  id: string;
  listId: string;
  userId: string;
  filename: string;
  status: 'processing' | 'completed' | 'failed';
  totalRows: number;
  processedRows: number;
  successCount: number;
  skipCount: number;
  errorCount: number;
  errors: ImportError[];
  mapping: Record<string, string>;
  startedAt: Date;
  completedAt?: Date;
}

export interface ImportError {
  row: number;
  email?: string;
  error: string;
  data?: Record<string, any>;
}

// ============================================================================
// CAMPAIGN MODELS
// ============================================================================

export interface Campaign {
  id: string;
  organizationId: string;
  name: string;
  subject: string;
  previewText?: string;
  fromName: string;
  fromEmail: string;
  replyTo?: string;
  status: 'draft' | 'scheduled' | 'sending' | 'sent' | 'paused' | 'cancelled';
  type: 'regular' | 'automated' | 'abtest';
  content: EmailContent;
  recipients: CampaignRecipients;
  schedule?: CampaignSchedule;
  statistics: CampaignStatistics;
  abTest?: ABTest;
  trackingSettings: TrackingSettings;
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
  scheduledAt?: Date;
  sentAt?: Date;
  completedAt?: Date;
}

export interface EmailContent {
  html: string;
  text: string;
  templateId?: string;
  variables?: Record<string, any>;
}

export interface CampaignRecipients {
  lists: string[];
  segments: string[];
  excludeLists: string[];
  excludeSegments?: string[];
  totalCount: number; // Calculated when sending
}

export interface CampaignSchedule {
  sendAt: Date;
  timezone: string;
  sendImmediately?: boolean;
}

export interface CampaignStatistics {
  sent: number;
  delivered: number;
  opens: number;
  uniqueOpens: number;
  clicks: number;
  uniqueClicks: number;
  bounces: number;
  softBounces: number;
  hardBounces: number;
  complaints: number;
  unsubscribes: number;
  performance: {
    openRate: number; // Percentage
    clickRate: number;
    clickToOpenRate: number;
    bounceRate: number;
    complaintRate: number;
    unsubscribeRate: number;
  };
  revenue?: number; // For e-commerce tracking
}

export interface ABTest {
  variants: ABTestVariant[];
  testType: 'subject' | 'from_name' | 'content';
  testDuration: number; // Hours
  winnerCriteria: 'openRate' | 'clickRate' | 'conversions';
  winnerVariantId?: string;
  status: 'testing' | 'completed';
  startedAt?: Date;
  completedAt?: Date;
}

export interface ABTestVariant {
  id: string;
  name: string;
  percentage: number; // 0-100
  subject?: string;
  fromName?: string;
  content?: EmailContent;
  statistics: CampaignStatistics;
}

export interface TrackingSettings {
  trackOpens: boolean;
  trackClicks: boolean;
  googleAnalytics?: {
    enabled: boolean;
    utmSource?: string;
    utmMedium?: string;
    utmCampaign?: string;
  };
}

// ============================================================================
// EMAIL ACTIVITY MODELS
// ============================================================================

export interface EmailActivity {
  id: string;
  campaignId: string;
  contactId: string;
  type: 'sent' | 'delivered' | 'opened' | 'clicked' | 'bounced' | 'complained' | 'unsubscribed';
  timestamp: Date;
  metadata?: Record<string, any>;
}

export interface EmailOpen {
  id: string;
  campaignId: string;
  contactId: string;
  openedAt: Date;
  userAgent?: string;
  ipAddress?: string;
  location?: ContactLocation;
}

export interface EmailClick {
  id: string;
  campaignId: string;
  contactId: string;
  url: string;
  clickedAt: Date;
  userAgent?: string;
  ipAddress?: string;
  location?: ContactLocation;
}

export interface EmailBounce {
  id: string;
  campaignId: string;
  contactId: string;
  type: 'soft' | 'hard';
  reason: string;
  bounceCode?: string;
  bouncedAt: Date;
}

export interface EmailUnsubscribe {
  id: string;
  campaignId?: string;
  contactId: string;
  reason?: string;
  feedback?: string;
  unsubscribedAt: Date;
  ipAddress?: string;
}

// ============================================================================
// TEMPLATE MODELS
// ============================================================================

export interface Template {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  category: 'promotional' | 'newsletter' | 'welcome' | 'transactional' | 'announcement' | 'event' | 'survey' | 'custom';
  thumbnail?: string;
  content: EmailContent;
  variables: TemplateVariable[];
  isArchived: boolean;
  isPublic: boolean; // For sharing templates
  usageCount: number;
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface TemplateVariable {
  name: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'image' | 'url';
  required: boolean;
  defaultValue?: any;
  placeholder?: string;
  validation?: {
    pattern?: string;
    minLength?: number;
    maxLength?: number;
    min?: number;
    max?: number;
  };
}

// ============================================================================
// AUTOMATION MODELS
// ============================================================================

export interface Automation {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  status: 'active' | 'paused' | 'draft';
  trigger: AutomationTrigger;
  actions: AutomationAction[];
  conditions?: AutomationCondition[];
  statistics: AutomationStatistics;
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
  lastRunAt?: Date;
}

export interface AutomationTrigger {
  type: 'contact_subscribed' | 'email_opened' | 'link_clicked' | 'date_reached' | 'custom_field_changed' | 'tag_added' | 'tag_removed' | 'segment_entered' | 'webhook_received';
  config: Record<string, any>;
}

export interface AutomationAction {
  id: string;
  type: 'send_email' | 'add_tag' | 'remove_tag' | 'wait' | 'webhook' | 'send_sms' | 'update_field' | 'add_to_list' | 'remove_from_list' | 'if_then_else';
  delay: {
    value: number;
    unit: 'minutes' | 'hours' | 'days' | 'weeks';
  };
  config: Record<string, any>;
  nextActionId?: string; // For branching logic
  conditions?: AutomationCondition[];
}

export interface AutomationCondition {
  field: string;
  operator: 'equals' | 'not_equals' | 'contains' | 'greater' | 'less';
  value: any;
}

export interface AutomationStatistics {
  totalRuns: number;
  activeRuns: number;
  completedRuns: number;
  failedRuns: number;
  avgCompletionTime: number; // In seconds
  conversionRate: number; // Percentage
  performance: {
    emailsSent: number;
    emailsOpened: number;
    emailsClicked: number;
  };
}

export interface AutomationRun {
  id: string;
  automationId: string;
  contactId: string;
  status: 'running' | 'completed' | 'failed' | 'cancelled';
  currentStepId?: string;
  currentStepIndex: number;
  totalSteps: number;
  data: Record<string, any>; // Context data for the run
  errors: AutomationError[];
  startedAt: Date;
  completedAt?: Date;
  nextRunAt?: Date; // For wait actions
}

export interface AutomationError {
  stepId: string;
  error: string;
  timestamp: Date;
  retryCount: number;
}

// ============================================================================
// BILLING & SUBSCRIPTION MODELS
// ============================================================================

export interface BillingPlan {
  id: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  interval: 'month' | 'year';
  intervalCount: number;
  limits: PlanLimits;
  features: string[];
  isActive: boolean;
  sortOrder: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface PlanLimits {
  contacts: number; // -1 for unlimited
  emailsPerMonth: number;
  users: number;
  automations: number;
  templates: number;
  apiCalls: number;
  storage: number; // In MB
  customFields: number;
  lists: number;
}

export interface Subscription {
  id: string;
  organizationId: string;
  planId: string;
  status: 'active' | 'trialing' | 'past_due' | 'canceled' | 'unpaid';
  currentPeriodStart: Date;
  currentPeriodEnd: Date;
  cancelAtPeriodEnd: boolean;
  canceledAt?: Date;
  trialStart?: Date;
  trialEnd?: Date;
  billingInterval: 'month' | 'year';
  price: number;
  currency: string;
  paymentMethodId?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface PaymentMethod {
  id: string;
  organizationId: string;
  type: 'card' | 'paypal' | 'bank_transfer';
  brand?: string; // e.g., 'visa', 'mastercard'
  last4?: string;
  expiryMonth?: number;
  expiryYear?: number;
  isDefault: boolean;
  billingDetails?: {
    name?: string;
    email?: string;
    address?: {
      line1?: string;
      line2?: string;
      city?: string;
      state?: string;
      postalCode?: string;
      country?: string;
    };
  };
  createdAt: Date;
  updatedAt: Date;
}

export interface Invoice {
  id: string;
  organizationId: string;
  subscriptionId: string;
  number: string; // e.g., 'INV-2024-001'
  status: 'draft' | 'open' | 'paid' | 'void' | 'uncollectible';
  amount: number;
  currency: string;
  tax: number;
  total: number;
  dueDate: Date;
  paidAt?: Date;
  items: InvoiceItem[];
  downloadUrl?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface InvoiceItem {
  description: string;
  quantity: number;
  unitPrice: number;
  amount: number;
}

// ============================================================================
// API KEY MODELS
// ============================================================================

export interface ApiKey {
  id: string;
  organizationId: string;
  name: string;
  key: string; // Hash this in the database!
  prefix: string; // e.g., 'ml_live_', 'ml_test_'
  permissions: string[];
  status: 'active' | 'revoked';
  lastUsedAt?: Date;
  expiresAt?: Date;
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface ApiKeyActivity {
  id: string;
  apiKeyId: string;
  endpoint: string;
  method: string;
  statusCode: number;
  responseTime: number; // In milliseconds
  ipAddress: string;
  userAgent: string;
  timestamp: Date;
}

// ============================================================================
// USAGE & ANALYTICS MODELS
// ============================================================================

export interface UsageStatistics {
  organizationId: string;
  period: Date; // Start of the period (e.g., first day of month)
  contacts: {
    total: number;
    active: number;
    added: number;
    removed: number;
  };
  emails: {
    sent: number;
    delivered: number;
    opened: number;
    clicked: number;
  };
  campaigns: {
    created: number;
    sent: number;
  };
  automations: {
    active: number;
    runs: number;
  };
  apiCalls: number;
  storage: number; // In MB
  createdAt: Date;
  updatedAt: Date;
}

export interface UsageAlert {
  id: string;
  organizationId: string;
  type: 'warning' | 'critical';
  resource: 'contacts' | 'emails' | 'storage' | 'api_calls';
  message: string;
  threshold: number; // Percentage
  currentUsage: number;
  limit: number;
  isAcknowledged: boolean;
  createdAt: Date;
  acknowledgedAt?: Date;
}

// ============================================================================
// WEBHOOK MODELS
// ============================================================================

export interface Webhook {
  id: string;
  organizationId: string;
  url: string;
  events: string[];
  status: 'active' | 'disabled' | 'failed';
  secret: string; // For signature verification
  headers?: Record<string, string>;
  retryPolicy: {
    maxRetries: number;
    retryDelay: number; // In seconds
  };
  lastTriggeredAt?: Date;
  failureCount: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface WebhookLog {
  id: string;
  webhookId: string;
  event: string;
  payload: Record<string, any>;
  statusCode?: number;
  response?: string;
  error?: string;
  attempts: number;
  succeededAt?: Date;
  failedAt?: Date;
  timestamp: Date;
}

export interface WebhookEvent {
  id: string;
  type: string; // e.g., 'contact.created', 'campaign.sent'
  data: Record<string, any>;
  timestamp: Date;
}

// ============================================================================
// INTEGRATION MODELS
// ============================================================================

export interface Integration {
  id: string;
  organizationId: string;
  type: 'zapier' | 'stripe' | 'shopify' | 'wordpress' | 'custom';
  name: string;
  status: 'active' | 'disconnected' | 'error';
  credentials: Record<string, any>; // Encrypted!
  config: Record<string, any>;
  lastSyncAt?: Date;
  errorMessage?: string;
  createdAt: Date;
  updatedAt: Date;
}

// ============================================================================
// FILE MODELS
// ============================================================================

export interface File {
  id: string;
  organizationId: string;
  userId: string;
  filename: string;
  originalFilename: string;
  mimeType: string;
  size: number; // In bytes
  url: string;
  path: string;
  type: 'avatar' | 'attachment' | 'import' | 'export' | 'image';
  metadata?: Record<string, any>;
  uploadedAt: Date;
}

// ============================================================================
// ORGANIZATION MODELS (Multi-tenancy)
// ============================================================================

export interface Organization {
  id: string;
  name: string;
  slug: string;
  logo?: string;
  website?: string;
  industry?: string;
  size?: string;
  subscriptionId?: string;
  settings: OrganizationSettings;
  createdAt: Date;
  updatedAt: Date;
}

export interface OrganizationSettings {
  timezone: string;
  language: string;
  dateFormat: string;
  currency: string;
  emailDomain?: string; // For custom sending domain
  defaultFromName?: string;
  defaultFromEmail?: string;
  unsubscribeFooter?: string;
}

// ============================================================================
// NOTIFICATION MODELS
// ============================================================================

export interface Notification {
  id: string;
  userId: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  actionUrl?: string;
  actionText?: string;
  isRead: boolean;
  readAt?: Date;
  createdAt: Date;
}

// ============================================================================
// AUDIT LOG MODELS
// ============================================================================

export interface AuditLog {
  id: string;
  organizationId: string;
  userId?: string;
  action: string; // e.g., 'contact.created', 'campaign.sent'
  resource: string; // e.g., 'contact', 'campaign'
  resourceId: string;
  changes?: {
    before?: Record<string, any>;
    after?: Record<string, any>;
  };
  ipAddress?: string;
  userAgent?: string;
  timestamp: Date;
}

// ============================================================================
// DATABASE ENUMS (dla SQL databases)
// ============================================================================

export enum UserRole {
  ADMIN = 'admin',
  USER = 'user',
  OWNER = 'owner'
}

export enum ContactStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  BOUNCED = 'bounced',
  UNSUBSCRIBED = 'unsubscribed'
}

export enum CampaignStatus {
  DRAFT = 'draft',
  SCHEDULED = 'scheduled',
  SENDING = 'sending',
  SENT = 'sent',
  PAUSED = 'paused',
  CANCELLED = 'cancelled'
}

export enum AutomationStatus {
  ACTIVE = 'active',
  PAUSED = 'paused',
  DRAFT = 'draft'
}

export enum SubscriptionStatus {
  ACTIVE = 'active',
  TRIALING = 'trialing',
  PAST_DUE = 'past_due',
  CANCELED = 'canceled',
  UNPAID = 'unpaid'
}

// ============================================================================
// API REQUEST/RESPONSE TYPES
// ============================================================================

export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  errors?: Record<string, string[]>;
  timestamp?: string;
}

export interface PaginatedResponse<T = any> {
  success: boolean;
  data: T[];
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasNextPage: boolean;
    hasPreviousPage: boolean;
  };
  message?: string;
}

export interface ApiError {
  success: false;
  message: string;
  errors?: Record<string, string[]>;
  timestamp: string;
  path?: string;
  statusCode: number;
}

// ============================================================================
// VALIDATION SCHEMAS (przykłady dla Joi/Zod/class-validator)
// ============================================================================

export interface ContactValidation {
  email: string; // required, valid email
  firstName: string; // required, min 2, max 50
  lastName: string; // required, min 2, max 50
  phone?: string; // optional, valid phone format
  tags?: string[]; // optional array
  customFields?: CustomField[]; // optional array
  status?: ContactStatus; // optional enum
}

export interface CampaignValidation {
  name: string; // required, min 3, max 100
  subject: string; // required, min 3, max 200
  fromName: string; // required, min 2, max 50
  fromEmail: string; // required, valid email
  content: EmailContent; // required, html and text
  recipients: CampaignRecipients; // required, at least one list or segment
}

// ============================================================================
// DATABASE INDEXES (rekomendacje)
// ============================================================================

/**
 * Rekomendowane indeksy dla głównych tabel:
 *
 * contacts:
 *   - idx_email (email) UNIQUE
 *   - idx_organization_status (organizationId, status)
 *   - idx_tags (tags) GIN/ARRAY index
 *   - idx_created_at (createdAt)
 *
 * campaigns:
 *   - idx_organization_status (organizationId, status)
 *   - idx_scheduled_at (scheduledAt)
 *   - idx_sent_at (sentAt)
 *
 * email_activities:
 *   - idx_campaign_contact (campaignId, contactId)
 *   - idx_type_timestamp (type, timestamp)
 *
 * automations:
 *   - idx_organization_status (organizationId, status)
 *
 * automation_runs:
 *   - idx_automation_contact (automationId, contactId)
 *   - idx_status_next_run (status, nextRunAt)
 */

// ============================================================================
// PRZYKŁAD UŻYCIA W BACKEND
// ============================================================================

/**
 * Node.js + Express przykład:
 *
 * import { Contact, ApiResponse } from './models';
 *
 * app.post('/api/v1/contacts', async (req, res) => {
 *   try {
 *     const contactData: Omit<Contact, 'id' | 'createdAt' | 'updatedAt'> = req.body;
 *
 *     // Validation
 *     const errors = validateContact(contactData);
 *     if (errors) {
 *       return res.status(422).json({
 *         success: false,
 *         message: 'Validation failed',
 *         errors
 *       });
 *     }
 *
 *     // Create contact
 *     const contact = await db.contacts.create({
 *       ...contactData,
 *       id: generateUuid(),
 *       organizationId: req.user.organizationId,
 *       createdAt: new Date(),
 *       updatedAt: new Date()
 *     });
 *
 *     // Response
 *     const response: ApiResponse<Contact> = {
 *       success: true,
 *       data: contact,
 *       message: 'Contact created successfully'
 *     };
 *
 *     res.status(201).json(response);
 *   } catch (error) {
 *     res.status(500).json({
 *       success: false,
 *       message: 'Internal server error'
 *     });
 *   }
 * });
 */
