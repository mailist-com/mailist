# Marketing Automation Platform

Nowoczesna platforma marketing automation zbudowana w Java 17 z Spring Boot 3.x, implementująca zasady DDD (Domain-Driven Design), SOLID i Clean Architecture.

## 🚀 Funkcjonalności

### 📧 E-mail Marketing
- Tworzenie i zarządzanie kampaniami e-mailowymi
- Planowanie wysyłki kampanii
- Integracja z EmailLabs do wysyłki e-maili
- Szablony HTML i tekstowe

### 🤖 Marketing Automation
- Tworzenie reguł automatyzacji z logiką if/else
- Reagowanie na zdarzenia (otwarcie e-maila, dodanie taga, itp.)
- Wykonywanie akcji automatycznych (wysyłka e-maili, dodawanie tagów)
- Silnik automatyzacji z obsługą warunków

### 👥 CRM / Kontakty / Listy
- Zarządzanie kontaktami
- System tagów i segmentacji
- Listy mailingowe (statyczne i dynamiczne)
- Lead scoring

### 📊 Analityka i Raportowanie
- Raporty skuteczności kampanii
- Analiza zaangażowania kontaktów
- Metryki automatyzacji
- Statystyki wzrostu bazy kontaktów

## 🏗️ Architektura

System zbudowany zgodnie z zasadami Clean Architecture i DDD:

```
src/main/java/com/mailist/marketing/
├── domain/           # Logika biznesowa
│   ├── aggregate/    # Agregaty (Campaign, Contact, AutomationRule, Report)
│   ├── valueobject/  # Obiekty wartości (EmailTemplate, Tag, Condition, Action)
│   ├── service/      # Serwisy domenowe
│   ├── gateway/      # Interfejsy gateway
│   └── model/        # Modele domenowe
├── application/      # Przypadki użycia
│   ├── usecase/      # Use case'y
│   └── port/         # Porty aplikacji
├── infrastructure/   # Implementacje techniczne
│   ├── repository/   # Implementacje repozytoriów
│   ├── gateway/      # Implementacje gateway (EmailLabs)
│   ├── config/       # Konfiguracje
│   └── data/         # Inicjalizacja danych
└── interfaces/       # API i kontrolery
    ├── controller/   # REST kontrolery
    ├── dto/          # DTOs
    ├── mapper/       # Mapowanie DTO ↔ Domain
    └── config/       # Konfiguracja OpenAPI
```

## 🛠️ Technologie

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Spring Security** (JWT)
- **PostgreSQL**
- **MapStruct** (mapowanie DTO)
- **WebClient** (komunikacja z EmailLabs)
- **OpenAPI 3** (dokumentacja API)
- **Testcontainers** (testy integracyjne)

## 🚦 Uruchomienie

### Wymagania
- Java 17+
- PostgreSQL
- Maven 3.6+

### Konfiguracja bazy danych
```sql
CREATE DATABASE marketing_automation;
```

### Zmienne środowiskowe
```bash
export EMAILLABS_API_KEY=your-api-key
export EMAILLABS_SECRET=your-secret
```

### Uruchomienie aplikacji
```bash
mvn spring-boot:run
```

### Dostęp do aplikacji
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 📝 API Endpoints

### Kampanie
- `POST /api/campaigns` - Tworzenie kampanii
- `GET /api/campaigns` - Lista kampanii
- `POST /api/campaigns/{id}/schedule` - Planowanie kampanii
- `POST /api/campaigns/{id}/send` - Wysyłka kampanii

### Kontakty
- `POST /api/contacts` - Tworzenie kontaktu
- `PUT /api/contacts/{id}/tags` - Dodawanie taga do kontaktu

### Automatyzacja
- `POST /api/automation/{id}/execute` - Wykonanie reguły automatyzacji

### Raporty
- `GET /api/reports/campaign/{campaignId}` - Raport kampanii

## 🔗 Integracja z EmailLabs

System wykorzystuje EmailLabs do wysyłki e-maili poprzez REST API. Konfiguracja w `application.yaml`:

```yaml
emaillabs:
  api-base-url: https://api.emaillabs.net.pl/v1
  api-key: ${EMAILLABS_API_KEY}
  secret: ${EMAILLABS_SECRET}
```

## 🎯 Przykłady użycia

### Tworzenie kampanii
```json
POST /api/campaigns
{
  "name": "Welcome Campaign",
  "subject": "Witamy!",
  "htmlContent": "<h1>Witamy!</h1>",
  "recipients": ["user@example.com"]
}
```

### Tworzenie kontaktu
```json
POST /api/contacts
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "email": "jan@example.com"
}
```

### Wykonanie automatyzacji
```bash
POST /api/automation/1/execute?contactId=1
{
  "emailOpened": true,
  "campaignId": "123"
}
```

## 🧪 Testy

```bash
# Uruchomienie testów
mvn test

# Testy integracyjne z Testcontainers
mvn verify
```

## 📊 Monitoring

Aplikacja udostępnia endpoint'y monitorowania:
- `/actuator/health` - Status zdrowia aplikacji
- `/actuator/metrics` - Metryki aplikacji
- `/actuator/prometheus` - Metryki w formacie Prometheus

## 🔒 Bezpieczeństwo

- JWT authentication
- HTTPS w środowisku produkcyjnym
- Walidacja danych wejściowych
- Ochrona przed atakami CSRF/XSS

## 📈 Przykładowe dane

Aplikacja automatycznie tworzy przykładowe dane:
- 3 kontakty testowe
- 2 listy kontaktów
- 2 kampanie e-mailowe
- 2 reguły automatyzacji

## 🤝 Rozwój

System został zaprojektowany jako modularny i rozszerzalny:
- Łatwe dodawanie nowych typów automatyzacji
- Możliwość integracji z innymi dostawcami e-mail
- Rozszerzalne raporty i analityka
- Pluggable architecture dla nowych funkcjonalności

## 📄 Licencja

MIT License