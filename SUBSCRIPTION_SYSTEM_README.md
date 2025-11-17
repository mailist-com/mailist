# System Subskrypcji Mailist - Dokumentacja

## 🎯 Przegląd

Kompleksowy system subskrypcji z trzema planami (FREE, STANDARD, PRO), dynamicznym cenowaniem, integracją Stripe i Fakturowni oraz automatycznym egzekwowaniem limitów.

## 📋 Plany Subskrypcyjne

### FREE
- **Kontakty**: 1,000
- **Maile/miesiąc**: 9,000
- **Użytkownicy**: 1
- **Kampanie**: 10/miesiąc
- **Automatyzacje**: 5
- **Cena**: 0 PLN

### STANDARD
- **Kontakty**: Bez limitu
- **Maile/miesiąc**: Bez limitu
- **Użytkownicy**: 3
- **Kampanie**: Bez limitu
- **Automatyzacje**: Bez limitu
- **Cena**: od 49 PLN + 49 PLN za każde kolejne 1000 kontaktów

### PRO
- **Wszystko**: Bez limitu
- **Cena**: od 99 PLN + 99 PLN za każde kolejne 1000 kontaktów

## 🏗️ Architektura

### Moduły
```
billing/
├── domain/
│   ├── aggregate/         # Domain aggregates (DDD)
│   ├── valueobject/       # Value objects (enums, etc.)
│   ├── repository/        # Repository interfaces
│   └── gateway/          # Payment & Invoicing provider interfaces
├── application/
│   └── service/          # Business logic services
├── infrastructure/
│   ├── gateway/          # Provider implementations
│   ├── scheduler/        # Scheduled jobs
│   └── config/          # Configuration
└── interfaces/
    ├── controller/       # REST API endpoints
    └── dto/             # Data Transfer Objects
```

### Wzorce Projektowe
- **Strategy Pattern**: Abstraction payment & invoicing providers
- **Factory Pattern**: Provider selection
- **DDD**: Domain-Driven Design architecture
- **Clean Architecture**: Separation of concerns

## 🔌 REST API Endpoints

### Subscription Management

#### Get Available Plans
```http
GET /api/v1/subscriptions/plans
```
Response: Lista dostępnych planów z cenami i limitami

#### Get Current Subscription
```http
GET /api/v1/subscriptions/current
Authorization: Bearer {token}
```
Response: Aktywna subskrypcja użytkownika

#### Create Subscription
```http
POST /api/v1/subscriptions
Authorization: Bearer {token}
Content-Type: application/json

{
  "planName": "STANDARD",
  "contactTier": 1,
  "billingCycle": "MONTHLY",
  "customerEmail": "user@example.com",
  "customerName": "Jan Kowalski",
  "paymentProvider": "stripe"
}
```

#### Change Subscription
```http
PUT /api/v1/subscriptions/change
Authorization: Bearer {token}
Content-Type: application/json

{
  "newPlanName": "PRO",
  "contactTier": 5
}
```

#### Cancel Subscription
```http
DELETE /api/v1/subscriptions
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Zbyt drogie",
  "immediately": false
}
```

#### Get Usage Statistics
```http
GET /api/v1/subscriptions/usage
Authorization: Bearer {token}
```
Response: Aktualne wykorzystanie zasobów z procentami

#### Get Payment History
```http
GET /api/v1/subscriptions/payments
Authorization: Bearer {token}
```

#### Get Invoice History
```http
GET /api/v1/subscriptions/invoices
Authorization: Bearer {token}
```

## 🔔 Webhook Integration

### Stripe Webhook Endpoint
```http
POST /api/webhooks/stripe
Stripe-Signature: {signature}
```

**Obsługiwane eventy:**
- `payment_intent.succeeded` - Płatność udana
- `payment_intent.payment_failed` - Płatność nieudana
- `customer.subscription.created` - Subskrypcja utworzona
- `customer.subscription.updated` - Subskrypcja zaktualizowana
- `customer.subscription.deleted` - Subskrypcja anulowana
- `invoice.paid` - Faktura opłacona
- `invoice.payment_failed` - Płatność faktury nieudana

### Konfiguracja Webhook w Stripe
1. Zaloguj się do Stripe Dashboard
2. Idź do **Developers → Webhooks**
3. Kliknij **Add endpoint**
4. URL: `https://your-domain.com/api/webhooks/stripe`
5. Wybierz eventy (wszystkie payment_intent.*, invoice.*, customer.subscription.*)
6. Skopiuj **Signing secret** i dodaj do zmiennych środowiskowych

## ⚙️ Konfiguracja

### Zmienne Środowiskowe

```bash
# Stripe
STRIPE_API_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Fakturownia
FAKTUROWNIA_API_TOKEN=your_api_token
FAKTUROWNIA_ACCOUNT_NAME=your_account_name
```

### application.yaml
```yaml
billing:
  stripe:
    api-key: ${STRIPE_API_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
    enabled: true

  fakturownia:
    api-token: ${FAKTUROWNIA_API_TOKEN}
    account-name: ${FAKTUROWNIA_ACCOUNT_NAME}
    api-url: https://ACCOUNT.fakturownia.pl
    enabled: true

  subscription:
    trial-period-days: 14
    grace-period-days: 3
    send-limit-warnings: true
    warning-thresholds:
      - 80
      - 90
      - 95
```

## 🚨 Egzekwowanie Limitów

### Automatyczne Sprawdzanie

System automatycznie sprawdza limity przy:
- **Dodawaniu kontaktu** (`CreateContactUseCase`)
- **Wysyłaniu kampanii** (`SendCampaignUseCase`)
- **Dodawaniu użytkownika** (TODO - do zaimplementowania)

### Powiadomienia

Użytkownicy otrzymują powiadomienia gdy wykorzystanie osiągnie:
- **80%** - Pierwsze ostrzeżenie
- **90%** - Drugie ostrzeżenie
- **95%** - Ostatnie ostrzeżenie
- **100%** - Blokada funkcjonalności

### Typy Powiadomień
- Email
- In-app notification (TODO)
- Push notification (TODO)

## 📊 Śledzenie Użycia

### UsageTracking
Śledzi miesięczne wykorzystanie zasobów:
- Liczba kontaktów (nie resetuje się)
- Wysłane maile (resetuje się miesięcznie)
- Utworzone kampanie (resetuje się miesięcznie)
- Aktywne automatyzacje
- Liczba użytkowników

### Scheduled Jobs

#### Miesięczny Reset (1. dzień miesiąca, 00:00)
```java
@Scheduled(cron = "0 0 0 1 * *")
public void resetMonthlyCounters()
```
Resetuje liczniki emaili i kampanii, zachowując liczbę kontaktów.

#### Wysyłanie Powiadomień (co godzinę)
```java
@Scheduled(cron = "0 0 * * * *")
public void sendPendingNotifications()
```
Wysyła oczekujące powiadomienia o limitach.

#### Sprawdzanie Wygasających Subskrypcji (codziennie, 09:00)
```java
@Scheduled(cron = "0 0 9 * * *")
public void checkExpiringSubscriptions()
```
Wysyła przypomnienia o odnowieniu subskrypcji.

## 💳 Flow Płatności

### 1. Utworzenie Subskrypcji
```
User → API: POST /subscriptions
API → SubscriptionService: createSubscription()
SubscriptionService → Stripe: Create Customer
SubscriptionService → Stripe: Create Subscription
Stripe → API: Return subscription ID
API → Database: Save subscription
API → User: Return subscription details
```

### 2. Płatność Recurring
```
Stripe → Webhook: payment_intent.succeeded
Webhook → SubscriptionService: processSuccessfulPayment()
SubscriptionService → Database: Save payment
SubscriptionService → Fakturownia: Generate invoice
Fakturownia → Email: Send invoice to customer
SubscriptionService → Database: Update next billing date
```

### 3. Fakturowanie
```
Payment Success → SubscriptionService
SubscriptionService → Fakturownia API: Create invoice
Fakturownia → Response: Invoice ID, PDF URL
SubscriptionService → Database: Save invoice record
Fakturownia → Email: Auto-send invoice
```

## 🔒 Zabezpieczenia

### Limity Subskrypcyjne
- Sprawdzanie przed każdą operacją
- Transakcyjna konsystencja
- Multi-tenant isolation

### Webhook Security
- Signature verification (Stripe HMAC)
- IP whitelist (opcjonalne)
- Request logging

### API Security
- JWT authentication
- Tenant isolation via SecurityUtils
- Input validation
- Rate limiting (TODO)

## 🧪 Testowanie

### Test Subscription Flow
```bash
# 1. Create subscription
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "STANDARD",
    "contactTier": 1,
    "billingCycle": "MONTHLY",
    "customerEmail": "test@example.com",
    "customerName": "Test User"
  }'

# 2. Check current usage
curl -X GET http://localhost:8080/api/v1/subscriptions/usage \
  -H "Authorization: Bearer {token}"

# 3. Test webhook (use Stripe CLI)
stripe listen --forward-to localhost:8080/api/webhooks/stripe
stripe trigger payment_intent.succeeded
```

### Test Stripe Integration
```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Login to Stripe
stripe login

# Forward webhooks to local
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Trigger test events
stripe trigger payment_intent.succeeded
stripe trigger customer.subscription.created
```

## 📈 Monitoring

### Metryki do Śledzenia
- Liczba aktywnych subskrypcji
- Conversion rate (FREE → STANDARD → PRO)
- Churn rate
- Average Revenue Per User (ARPU)
- Miesięczne przychody (MRR)
- Wykorzystanie limitów (średnie)

### Logi
```bash
# Subscription events
grep "subscription" logs/app.log

# Payment events
grep "payment" logs/app.log

# Limit violations
grep "limit exceeded" logs/app.log
```

## 🚀 Deployment

### Production Checklist
- [ ] Ustaw zmienne środowiskowe (STRIPE_API_KEY, FAKTUROWNIA_API_TOKEN)
- [ ] Skonfiguruj webhook URL w Stripe Dashboard
- [ ] Włącz HTTPS dla webhooków
- [ ] Skonfiguruj email notifications
- [ ] Skonfiguruj monitoring i alerty
- [ ] Test payment flow end-to-end
- [ ] Backup bazy danych przed uruchomieniem

### Database Migration
```bash
# Liquibase automatycznie uruchomi migracje przy starcie
./mvnw spring-boot:run

# Sprawdź status migracji
./mvnw liquibase:status
```

## 🔮 Przyszłe Rozszerzenia

### Możliwe do Dodania
1. **PayPal Integration** - dodaj PayPalPaymentProvider
2. **Przelewy24 Integration** - dodaj Przelewy24PaymentProvider
3. **InFakt Integration** - dodaj InFaktInvoicingProvider
4. **Promocje i Kody Rabatowe** - discount system
5. **Trial Period** - 14-dniowy trial dla nowych użytkowników
6. **Annual Billing Discount** - rabat za roczną płatność
7. **Usage-based Pricing** - płatność za faktyczne wykorzystanie
8. **Add-ons** - dodatkowe funkcje do dokupienia

### Przykład Dodania Nowego Providera
```java
@Component
public class PayPalPaymentProvider implements PaymentProvider {
    @Override
    public String getProviderName() {
        return "paypal";
    }

    // Implementuj pozostałe metody...
}
```
Provider zostanie automatycznie zarejestrowany przez `PaymentProviderFactory`!

## 📞 Support

### Problemy z Płatnościami
1. Sprawdź logi webhooków
2. Zweryfikuj konfigurację Stripe
3. Sprawdź status płatności w Stripe Dashboard

### Problemy z Fakturami
1. Sprawdź połączenie z Fakturownia API
2. Zweryfikuj API token
3. Sprawdź logi generowania faktur

## 📄 License

Copyright © 2025 Mailist. All rights reserved.
