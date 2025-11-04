# Mailist Backend API - Dokumentacja dla Zespołu Developerskiego

## 📋 Spis Treści

1. [Wprowadzenie](#wprowadzenie)
2. [Konfiguracja Środowiska](#konfiguracja-środowiska)
3. [Autentykacja](#autentykacja)
4. [Standardy Odpowiedzi API](#standardy-odpowiedzi-api)
5. [Endpointy API](#endpointy-api)
6. [Modele Danych](#modele-danych)
7. [Kody Błędów](#kody-błędów)
8. [Przykłady Integracji](#przykłady-integracji)

---

## Wprowadzenie

Niniejsza dokumentacja opisuje wszystkie wymagane endpointy API dla platformy Mailist - systemu email marketingu. Frontend jest zbudowany w Angular 20 i oczekuje RESTful API zgodnego z poniższą specyfikacją.

### Podstawowe Informacje

- **Wersja API:** v1
- **Base URL (produkcja):** `https://api.mailist.com/api/v1`
- **Base URL (development):** `http://localhost:3000/api/v1`
- **Format danych:** JSON
- **Autentykacja:** Bearer Token (JWT)
- **Kodowanie:** UTF-8

---

## Konfiguracja Środowiska

### Wymagane Nagłówki

Wszystkie żądania do API powinny zawierać następujące nagłówki:

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}
X-API-Version: v1
```

### Timeouty

- **Standard:** 30 sekund
- **Upload plików:** 60 sekund
- **Export danych:** 60 sekund

---

## Autentykacja

### 1. Logowanie

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "rememberMe": false
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid-string",
      "email": "user@example.com",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "role": "admin",
      "avatar": "https://example.com/avatar.jpg"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh-token-here"
  },
  "message": "Login successful"
}
```

**Błędy:**
- `401 Unauthorized` - Nieprawidłowe dane logowania
- `422 Unprocessable Entity` - Błąd walidacji

---

### 2. Rejestracja

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "email": "jan@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "acceptTerms": true
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid-string",
      "email": "jan@example.com",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "role": "user",
      "avatar": null
    },
    "token": "jwt-token",
    "refreshToken": "refresh-token"
  },
  "message": "Registration successful"
}
```

**Walidacja:**
- Email musi być unikalny
- Hasło minimum 8 znaków
- acceptTerms musi być true

---

### 3. Odświeżenie Tokena

**Endpoint:** `POST /auth/refresh-token`

**Request Body:**
```json
{
  "refreshToken": "refresh-token-string"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "token": "new-jwt-token"
  }
}
```

---

### 4. Wylogowanie

**Endpoint:** `POST /auth/logout`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logout successful"
}
```

---

### 5. Reset Hasła

**Endpoint:** `POST /auth/password-reset`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "message": "Password reset instructions sent to email"
  }
}
```

---

### 6. Aktualizacja Hasła

**Endpoint:** `POST /auth/password-update`

**Request Body:**
```json
{
  "token": "reset-token-from-email",
  "password": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Response:** `200 OK`

---

### 7. Weryfikacja 2FA

**Endpoint:** `POST /auth/verify-2fa`

**Request Body:**
```json
{
  "code": "123456",
  "userId": "user-id"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "verified": true
  }
}
```

---

## Standardy Odpowiedzi API

### Format Standardowej Odpowiedzi

```typescript
{
  "success": boolean,
  "data": T,
  "message"?: string,
  "errors"?: Record<string, string[]>,
  "timestamp"?: string
}
```

### Format Odpowiedzi Paginowanej

```typescript
{
  "success": boolean,
  "data": T[],
  "pagination": {
    "page": number,
    "pageSize": number,
    "total": number,
    "totalPages": number,
    "hasNextPage": boolean,
    "hasPreviousPage": boolean
  },
  "message"?: string
}
```

### Przykład Paginowanej Odpowiedzi

```json
{
  "success": true,
  "data": [...],
  "pagination": {
    "page": 1,
    "pageSize": 25,
    "total": 150,
    "totalPages": 6,
    "hasNextPage": true,
    "hasPreviousPage": false
  }
}
```

---

## Endpointy API

---

## 📧 CONTACTS (Kontakty)

### 1. Pobierz Listę Kontaktów

**Endpoint:** `GET /contacts`

**Query Parameters:**
```
?page=1
&pageSize=25
&search=jan
&status=active,bounced
&subscriptionStatus=subscribed
&lists=list-id-1,list-id-2
&tags=premium,vip
&sortBy=createdAt
&sortOrder=desc
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "contact-uuid",
      "email": "jan@example.com",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "phone": "+48123456789",
      "organization": "Firma Sp. z o.o.",
      "tags": ["premium", "vip"],
      "customFields": [
        {
          "id": "cf1",
          "name": "Stanowisko",
          "value": "Dyrektor",
          "type": "text"
        }
      ],
      "lists": ["list-id-1", "list-id-2"],
      "status": "active",
      "subscriptionStatus": "subscribed",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-11-01T14:20:00Z",
      "lastActivity": "2024-11-05T09:15:00Z",
      "engagementScore": 85,
      "location": {
        "country": "Poland",
        "state": "Mazowieckie",
        "city": "Warszawa",
        "timezone": "Europe/Warsaw"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 25,
    "total": 150,
    "totalPages": 6
  }
}
```

---

### 2. Pobierz Pojedynczy Kontakt

**Endpoint:** `GET /contacts/{id}`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "contact-uuid",
    "email": "jan@example.com",
    // ... pełne dane kontaktu
  }
}
```

**Błędy:**
- `404 Not Found` - Kontakt nie istnieje

---

### 3. Utwórz Nowy Kontakt

**Endpoint:** `POST /contacts`

**Request Body:**
```json
{
  "email": "nowy@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48123456789",
  "organization": "Firma Sp. z o.o.",
  "tags": ["nowy"],
  "customFields": [],
  "lists": ["list-id"],
  "status": "active",
  "subscriptionStatus": "subscribed",
  "location": {
    "country": "Poland",
    "city": "Warszawa"
  }
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "new-contact-uuid",
    // ... utworzony kontakt
  },
  "message": "Contact created successfully"
}
```

**Walidacja:**
- Email jest wymagany i musi być unikalny
- Status: active, inactive, bounced, unsubscribed
- subscriptionStatus: subscribed, unsubscribed, pending

---

### 4. Aktualizuj Kontakt

**Endpoint:** `PUT /contacts/{id}`

**Request Body:** (częściowa aktualizacja)
```json
{
  "firstName": "Jan",
  "tags": ["premium", "vip", "updated"]
}
```

**Response:** `200 OK`

---

### 5. Usuń Kontakt

**Endpoint:** `DELETE /contacts/{id}`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Contact deleted successfully"
}
```

---

### 6. Dodaj Tag do Kontaktu

**Endpoint:** `POST /contacts/{id}/tags`

**Request Body:**
```json
{
  "tag": "vip"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    // zaktualizowany kontakt z nowym tagiem
  }
}
```

---

### 7. Usuń Tag z Kontaktu

**Endpoint:** `DELETE /contacts/{id}/tags/{tag}`

**Response:** `200 OK`

---

### 8. Wyszukaj Kontakty

**Endpoint:** `GET /contacts/search?q={query}`

**Query Parameters:**
```
?q=jan+kowalski
```

**Response:** `200 OK` - tablica kontaktów

---

### 9. Statystyki Kontaktów

**Endpoint:** `GET /contacts/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "total": 5000,
    "active": 4500,
    "unsubscribed": 300,
    "bounced": 200,
    "tagged": 2500,
    "growth": 15.5,
    "trend": "up"
  }
}
```

---

## 📋 CONTACT LISTS (Listy Kontaktów)

### 1. Pobierz Wszystkie Listy

**Endpoint:** `GET /lists`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "list-uuid",
      "name": "Newsletter Subscribers",
      "description": "Główna lista subskrybentów newslettera",
      "type": "standard",
      "subscriberCount": 2543,
      "isSmartList": false,
      "conditions": [],
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-11-05T10:00:00Z"
    }
  ]
}
```

---

### 2. Utwórz Listę

**Endpoint:** `POST /lists`

**Request Body:**
```json
{
  "name": "Nowa Lista",
  "description": "Opis listy",
  "type": "standard",
  "isSmartList": false
}
```

**Response:** `201 Created`

---

### 3. Utwórz Inteligentną Listę (Smart List)

**Endpoint:** `POST /lists/smart`

**Request Body:**
```json
{
  "name": "Active Developers",
  "description": "Kontakty z tagiem developer i aktywnością",
  "conditions": [
    {
      "field": "tags",
      "operator": "contains",
      "value": "developer"
    },
    {
      "field": "status",
      "operator": "equals",
      "value": "active"
    },
    {
      "field": "engagementScore",
      "operator": "greater",
      "value": 50
    }
  ]
}
```

**Dostępne operatory:**
- `equals`, `not_equals`
- `contains`, `not_contains`
- `greater`, `less`, `greater_or_equal`, `less_or_equal`
- `starts_with`, `ends_with`
- `is_empty`, `is_not_empty`

**Response:** `201 Created`

---

### 4. Subskrybuj Kontakt do Listy

**Endpoint:** `POST /lists/{listId}/subscribe`

**Request Body:**
```json
{
  "contactIds": ["contact-id-1", "contact-id-2"]
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "subscribed": 2,
    "failed": 0
  }
}
```

---

### 5. Wypisz Kontakt z Listy

**Endpoint:** `POST /lists/{listId}/unsubscribe`

**Request Body:**
```json
{
  "contactIds": ["contact-id-1"]
}
```

**Response:** `200 OK`

---

### 6. Import Kontaktów z CSV

**Endpoint:** `POST /lists/{listId}/import`

**Content-Type:** `multipart/form-data`

**Form Data:**
```
file: (CSV file)
mapping: {
  "email": "Email",
  "firstName": "First Name",
  "lastName": "Last Name"
}
skipDuplicates: true
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "imported": 150,
    "skipped": 10,
    "errors": 2,
    "errorDetails": [
      {
        "row": 5,
        "email": "invalid-email",
        "error": "Invalid email format"
      }
    ]
  }
}
```

---

### 7. Export Kontaktów do CSV

**Endpoint:** `GET /lists/{listId}/export?format=csv`

**Query Parameters:**
```
?format=csv
&fields=email,firstName,lastName,tags
```

**Response:** `200 OK`
- Content-Type: `text/csv`
- Plik CSV do pobrania

---

### 8. Statystyki Listy

**Endpoint:** `GET /lists/{listId}/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalSubscribers": 2543,
    "activeSubscribers": 2301,
    "unsubscribedToday": 5,
    "growthRate": 12.5,
    "avgEngagementScore": 67.8
  }
}
```

---

## 📨 CAMPAIGNS (Kampanie)

### 1. Pobierz Wszystkie Kampanie

**Endpoint:** `GET /campaigns`

**Query Parameters:**
```
?status=draft,sent,scheduled
&page=1
&pageSize=25
&sortBy=createdAt
&sortOrder=desc
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "campaign-uuid",
      "name": "Newsletter Listopad 2024",
      "subject": "Najnowsze trendy w email marketingu",
      "previewText": "Zobacz, co przygotowaliśmy...",
      "fromName": "Mailist Team",
      "fromEmail": "hello@mailist.com",
      "replyTo": "support@mailist.com",
      "status": "sent",
      "type": "regular",
      "content": {
        "html": "<html>...</html>",
        "text": "Plain text version..."
      },
      "recipients": {
        "lists": ["list-id-1"],
        "segments": ["segment-id-1"],
        "excludeLists": [],
        "totalCount": 2543
      },
      "schedule": {
        "sendAt": "2024-11-05T10:00:00Z",
        "timezone": "Europe/Warsaw"
      },
      "statistics": {
        "sent": 2543,
        "delivered": 2521,
        "opens": 1134,
        "uniqueOpens": 987,
        "clicks": 342,
        "uniqueClicks": 298,
        "bounces": 22,
        "complaints": 3,
        "unsubscribes": 15,
        "performance": {
          "openRate": 39.2,
          "clickRate": 13.5,
          "bounceRate": 0.9,
          "unsubscribeRate": 0.6
        }
      },
      "abTest": null,
      "createdAt": "2024-10-25T10:00:00Z",
      "updatedAt": "2024-11-05T10:00:00Z",
      "sentAt": "2024-11-05T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 25,
    "total": 45,
    "totalPages": 2
  }
}
```

**Statusy kampanii:**
- `draft` - szkic
- `scheduled` - zaplanowana
- `sending` - w trakcie wysyłania
- `sent` - wysłana
- `paused` - wstrzymana
- `cancelled` - anulowana

---

### 2. Pobierz Pojedynczą Kampanię

**Endpoint:** `GET /campaigns/{id}`

**Response:** `200 OK`

---

### 3. Utwórz Kampanię

**Endpoint:** `POST /campaigns`

**Request Body:**
```json
{
  "name": "Newsletter Listopad",
  "subject": "Temat wiadomości",
  "previewText": "Tekst podglądu",
  "fromName": "Mailist",
  "fromEmail": "hello@mailist.com",
  "replyTo": "support@mailist.com",
  "type": "regular",
  "content": {
    "html": "<html>...</html>",
    "text": "Plain text..."
  },
  "recipients": {
    "lists": ["list-id"],
    "segments": [],
    "excludeLists": []
  },
  "templateId": "template-uuid"
}
```

**Response:** `201 Created`

---

### 4. Aktualizuj Kampanię

**Endpoint:** `PUT /campaigns/{id}`

**Note:** Tylko kampanie w statusie `draft` mogą być edytowane

**Response:** `200 OK`

---

### 5. Wyślij Kampanię Natychmiast

**Endpoint:** `POST /campaigns/{id}/send`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "campaignId": "campaign-uuid",
    "status": "sending",
    "estimatedSendTime": "5 minutes"
  }
}
```

---

### 6. Zaplanuj Kampanię

**Endpoint:** `POST /campaigns/{id}/schedule`

**Request Body:**
```json
{
  "sendAt": "2024-11-10T10:00:00Z",
  "timezone": "Europe/Warsaw"
}
```

**Response:** `200 OK`

---

### 7. Wstrzymaj Kampanię

**Endpoint:** `POST /campaigns/{id}/pause`

**Note:** Tylko kampanie w statusie `sending` lub `scheduled`

**Response:** `200 OK`

---

### 8. Wznów Kampanię

**Endpoint:** `POST /campaigns/{id}/resume`

**Response:** `200 OK`

---

### 9. Duplikuj Kampanię

**Endpoint:** `POST /campaigns/{id}/duplicate`

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "new-campaign-uuid",
    "name": "Newsletter Listopad (kopia)"
  }
}
```

---

### 10. Usuń Kampanię

**Endpoint:** `DELETE /campaigns/{id}`

**Note:** Nie można usunąć kampanii w statusie `sending`

**Response:** `200 OK`

---

### 11. Statystyki Kampanii

**Endpoint:** `GET /campaigns/{id}/stats`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "sent": 2543,
    "delivered": 2521,
    "opens": 1134,
    "clicks": 342,
    "performance": {
      "openRate": 45.0,
      "clickRate": 13.5
    },
    "timeline": [
      {
        "timestamp": "2024-11-05T10:00:00Z",
        "opens": 150,
        "clicks": 45
      }
    ]
  }
}
```

---

### 12. Raport Otwarć

**Endpoint:** `GET /campaigns/{id}/open-report`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "contactId": "contact-uuid",
      "email": "jan@example.com",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "openCount": 3,
      "firstOpenedAt": "2024-11-05T10:15:00Z",
      "lastOpenedAt": "2024-11-05T14:30:00Z"
    }
  ]
}
```

---

### 13. Raport Kliknięć

**Endpoint:** `GET /campaigns/{id}/click-report`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "url": "https://example.com/product",
      "clicks": 125,
      "uniqueClicks": 98,
      "contacts": [
        {
          "contactId": "contact-uuid",
          "email": "jan@example.com",
          "clickCount": 2,
          "firstClickedAt": "2024-11-05T10:20:00Z"
        }
      ]
    }
  ]
}
```

---

### 14. Konfiguracja Testu A/B

**Endpoint:** `POST /campaigns/{id}/ab-test`

**Request Body:**
```json
{
  "testType": "subject",
  "variants": [
    {
      "name": "Wariant A",
      "subject": "Testowy temat A",
      "percentage": 50
    },
    {
      "name": "Wariant B",
      "subject": "Testowy temat B",
      "percentage": 50
    }
  ],
  "testDuration": 2,
  "winnerCriteria": "openRate"
}
```

**Typy testów:**
- `subject` - test tematów
- `fromName` - test nazwy nadawcy
- `content` - test treści

**Kryteria zwycięzcy:**
- `openRate` - wskaźnik otwarć
- `clickRate` - wskaźnik kliknięć
- `conversions` - konwersje

**Response:** `200 OK`

---

## 📄 TEMPLATES (Szablony)

### 1. Pobierz Wszystkie Szablony

**Endpoint:** `GET /templates`

**Query Parameters:**
```
?category=promotional,newsletter,transactional
&page=1
&pageSize=25
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "template-uuid",
      "name": "Newsletter Template",
      "description": "Szablon do newsletterów",
      "category": "newsletter",
      "thumbnail": "https://example.com/thumbnail.jpg",
      "content": {
        "html": "<html>...</html>",
        "text": "Plain text..."
      },
      "variables": [
        {
          "name": "firstName",
          "label": "Imię",
          "type": "text",
          "required": true
        },
        {
          "name": "companyName",
          "label": "Nazwa firmy",
          "type": "text",
          "required": false
        }
      ],
      "isArchived": false,
      "usageCount": 25,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-11-01T00:00:00Z"
    }
  ]
}
```

**Kategorie szablonów:**
- `promotional` - promocyjne
- `newsletter` - newslettery
- `welcome` - powitalne
- `transactional` - transakcyjne
- `announcement` - ogłoszenia
- `event` - wydarzenia
- `survey` - ankiety

---

### 2. Pobierz Szablon

**Endpoint:** `GET /templates/{id}`

**Response:** `200 OK`

---

### 3. Utwórz Szablon

**Endpoint:** `POST /templates`

**Request Body:**
```json
{
  "name": "Nowy Szablon",
  "description": "Opis szablonu",
  "category": "newsletter",
  "content": {
    "html": "<html>...</html>",
    "text": "Plain text..."
  },
  "variables": []
}
```

**Response:** `201 Created`

---

### 4. Aktualizuj Szablon

**Endpoint:** `PUT /templates/{id}`

**Response:** `200 OK`

---

### 5. Usuń Szablon

**Endpoint:** `DELETE /templates/{id}`

**Response:** `200 OK`

---

### 6. Duplikuj Szablon

**Endpoint:** `POST /templates/{id}/duplicate`

**Response:** `201 Created`

---

### 7. Archiwizuj Szablon

**Endpoint:** `POST /templates/{id}/archive`

**Response:** `200 OK`

---

### 8. Przywróć Szablon

**Endpoint:** `POST /templates/{id}/activate`

**Response:** `200 OK`

---

### 9. Statystyki Szablonów

**Endpoint:** `GET /templates/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "total": 50,
    "active": 45,
    "archived": 5,
    "mostUsed": [
      {
        "id": "template-uuid",
        "name": "Newsletter Template",
        "usageCount": 125
      }
    ]
  }
}
```

---

## ⚙️ AUTOMATIONS (Automatyzacje)

### 1. Pobierz Wszystkie Automatyzacje

**Endpoint:** `GET /automations`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "automation-uuid",
      "name": "Welcome Series",
      "description": "Seria powitalnych wiadomości",
      "status": "active",
      "trigger": {
        "type": "contact_subscribed",
        "config": {
          "listIds": ["list-id"]
        }
      },
      "actions": [
        {
          "id": "action-1",
          "type": "send_email",
          "delay": {
            "value": 0,
            "unit": "minutes"
          },
          "config": {
            "templateId": "template-uuid",
            "subject": "Witaj w Mailist!"
          }
        },
        {
          "id": "action-2",
          "type": "wait",
          "delay": {
            "value": 2,
            "unit": "days"
          }
        },
        {
          "id": "action-3",
          "type": "send_email",
          "delay": {
            "value": 0,
            "unit": "minutes"
          },
          "config": {
            "templateId": "template-uuid-2",
            "subject": "Jak wykorzystać Mailist?"
          }
        }
      ],
      "statistics": {
        "totalRuns": 543,
        "activeRuns": 125,
        "completedRuns": 418,
        "avgCompletionTime": "5 days",
        "conversionRate": 23.5
      },
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-11-01T00:00:00Z",
      "lastRunAt": "2024-11-05T09:30:00Z"
    }
  ]
}
```

**Statusy automatyzacji:**
- `active` - aktywna
- `paused` - wstrzymana
- `draft` - szkic

---

### 2. Typy Wyzwalaczy (Triggers)

**Endpoint:** `GET /automations/trigger-types`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "type": "contact_subscribed",
      "name": "Kontakt subskrybował",
      "description": "Uruchamia się gdy kontakt subskrybuje listę",
      "configSchema": {
        "listIds": "array"
      }
    },
    {
      "type": "email_opened",
      "name": "Email otwarty",
      "description": "Uruchamia się gdy kontakt otwiera email"
    },
    {
      "type": "link_clicked",
      "name": "Link kliknięty",
      "description": "Uruchamia się gdy kontakt klika link w emailu"
    },
    {
      "type": "date_reached",
      "name": "Data osiągnięta",
      "description": "Uruchamia się w określonym dniu/czasie"
    },
    {
      "type": "custom_field_changed",
      "name": "Pole niestandardowe zmienione",
      "description": "Uruchamia się gdy zmienia się wartość pola"
    },
    {
      "type": "tag_added",
      "name": "Tag dodany",
      "description": "Uruchamia się gdy dodano tag do kontaktu"
    }
  ]
}
```

---

### 3. Typy Akcji (Actions)

**Endpoint:** `GET /automations/action-types`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "type": "send_email",
      "name": "Wyślij email",
      "description": "Wysyła email do kontaktu",
      "configSchema": {
        "templateId": "string",
        "subject": "string"
      }
    },
    {
      "type": "add_tag",
      "name": "Dodaj tag",
      "description": "Dodaje tag do kontaktu"
    },
    {
      "type": "remove_tag",
      "name": "Usuń tag",
      "description": "Usuwa tag z kontaktu"
    },
    {
      "type": "wait",
      "name": "Czekaj",
      "description": "Czeka określony czas przed następną akcją"
    },
    {
      "type": "webhook",
      "name": "Webhook",
      "description": "Wywołuje zewnętrzny webhook"
    },
    {
      "type": "send_sms",
      "name": "Wyślij SMS",
      "description": "Wysyła SMS do kontaktu"
    },
    {
      "type": "update_field",
      "name": "Aktualizuj pole",
      "description": "Aktualizuje pole kontaktu"
    }
  ]
}
```

---

### 4. Utwórz Automatyzację

**Endpoint:** `POST /automations`

**Request Body:**
```json
{
  "name": "Welcome Series",
  "description": "Seria powitalnych wiadomości",
  "trigger": {
    "type": "contact_subscribed",
    "config": {
      "listIds": ["list-id"]
    }
  },
  "actions": [
    {
      "type": "send_email",
      "delay": {
        "value": 0,
        "unit": "minutes"
      },
      "config": {
        "templateId": "template-uuid",
        "subject": "Witaj!"
      }
    }
  ]
}
```

**Response:** `201 Created`

---

### 5. Aktywuj Automatyzację

**Endpoint:** `POST /automations/{id}/activate`

**Response:** `200 OK`

---

### 6. Wstrzymaj Automatyzację

**Endpoint:** `POST /automations/{id}/pause`

**Response:** `200 OK`

---

### 7. Duplikuj Automatyzację

**Endpoint:** `POST /automations/{id}/duplicate`

**Response:** `201 Created`

---

### 8. Historia Uruchomień

**Endpoint:** `GET /automations/{id}/runs`

**Query Parameters:**
```
?page=1
&pageSize=25
&status=completed,running,failed
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "run-uuid",
      "automationId": "automation-uuid",
      "contactId": "contact-uuid",
      "status": "completed",
      "startedAt": "2024-11-05T10:00:00Z",
      "completedAt": "2024-11-07T10:00:00Z",
      "currentStep": 3,
      "totalSteps": 3,
      "errors": []
    }
  ]
}
```

---

### 9. Statystyki Automatyzacji

**Endpoint:** `GET /automations/{id}/stats`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalRuns": 543,
    "activeRuns": 125,
    "completedRuns": 418,
    "failedRuns": 0,
    "avgCompletionTime": "5 days",
    "conversionRate": 23.5,
    "performance": {
      "emails_sent": 1629,
      "emails_opened": 1156,
      "emails_clicked": 487
    }
  }
}
```

---

## 👥 USER & TEAM (Użytkownik i Zespół)

### 1. Pobierz Profil Użytkownika

**Endpoint:** `GET /users/profile`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "jan@example.com",
    "firstName": "Jan",
    "lastName": "Kowalski",
    "role": "admin",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "+48123456789",
    "company": "Firma Sp. z o.o.",
    "timezone": "Europe/Warsaw",
    "language": "pl",
    "preferences": {
      "emailNotifications": true,
      "smsNotifications": false,
      "weeklyReport": true
    },
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

---

### 2. Aktualizuj Profil

**Endpoint:** `PUT /users/profile`

**Request Body:**
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48123456789",
  "timezone": "Europe/Warsaw"
}
```

**Response:** `200 OK`

---

### 3. Zmień Hasło

**Endpoint:** `POST /users/change-password`

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass123!",
  "confirmPassword": "NewPass123!"
}
```

**Response:** `200 OK`

---

### 4. Upload Avatara

**Endpoint:** `POST /users/avatar`

**Content-Type:** `multipart/form-data`

**Form Data:**
```
avatar: (image file)
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "avatarUrl": "https://example.com/avatars/user-uuid.jpg"
  }
}
```

---

### 5. Pobierz Preferencje

**Endpoint:** `GET /users/preferences`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "emailNotifications": true,
    "smsNotifications": false,
    "weeklyReport": true,
    "language": "pl",
    "timezone": "Europe/Warsaw"
  }
}
```

---

### 6. Zaktualizuj Preferencje

**Endpoint:** `PUT /users/preferences`

**Request Body:**
```json
{
  "emailNotifications": false,
  "weeklyReport": true
}
```

**Response:** `200 OK`

---

### 7. Pobierz Członków Zespołu

**Endpoint:** `GET /team/members`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "member-uuid",
      "email": "anna@example.com",
      "firstName": "Anna",
      "lastName": "Nowak",
      "role": "member",
      "permissions": [
        "campaigns.read",
        "campaigns.write",
        "contacts.read"
      ],
      "status": "active",
      "invitedAt": "2024-01-15T00:00:00Z",
      "joinedAt": "2024-01-15T10:30:00Z",
      "lastActiveAt": "2024-11-05T09:15:00Z"
    }
  ]
}
```

**Role zespołu:**
- `owner` - właściciel (pełny dostęp)
- `admin` - administrator (prawie pełny dostęp)
- `member` - członek (ograniczony dostęp)
- `viewer` - przeglądający (tylko odczyt)

---

### 8. Zaproś Członka Zespołu

**Endpoint:** `POST /team/invite`

**Request Body:**
```json
{
  "email": "nowy@example.com",
  "firstName": "Nowy",
  "lastName": "Członek",
  "role": "member",
  "permissions": [
    "campaigns.read",
    "campaigns.write"
  ]
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "invitationId": "invite-uuid",
    "email": "nowy@example.com",
    "expiresAt": "2024-11-12T10:00:00Z"
  },
  "message": "Invitation sent successfully"
}
```

---

### 9. Usuń Członka Zespołu

**Endpoint:** `DELETE /team/members/{id}`

**Response:** `200 OK`

---

### 10. Zmień Rolę Członka

**Endpoint:** `PUT /team/members/{id}/role`

**Request Body:**
```json
{
  "role": "admin",
  "permissions": [
    "campaigns.read",
    "campaigns.write",
    "contacts.read",
    "contacts.write"
  ]
}
```

**Response:** `200 OK`

---

### 11. Statystyki Zespołu

**Endpoint:** `GET /team/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalMembers": 5,
    "activeMembers": 4,
    "pendingInvitations": 1,
    "activity": {
      "campaignsSent": 45,
      "contactsAdded": 1250,
      "templatesCreated": 12
    }
  }
}
```

---

## 💳 BILLING (Płatności i Subskrypcje)

### 1. Pobierz Plany

**Endpoint:** `GET /billing/plans`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "plan-free",
      "name": "Free",
      "price": 0,
      "currency": "PLN",
      "interval": "month",
      "limits": {
        "contacts": 500,
        "emailsPerMonth": 1000,
        "users": 1,
        "automations": 1,
        "templates": 5,
        "apiCalls": 1000
      },
      "features": [
        "Email campaigns",
        "Basic templates",
        "Contact management"
      ]
    },
    {
      "id": "plan-standard",
      "name": "Standard",
      "price": 99,
      "currency": "PLN",
      "interval": "month",
      "limits": {
        "contacts": 10000,
        "emailsPerMonth": 50000,
        "users": 5,
        "automations": 10,
        "templates": 50,
        "apiCalls": 50000
      },
      "features": [
        "All Free features",
        "Advanced automations",
        "A/B testing",
        "Priority support"
      ]
    },
    {
      "id": "plan-professional",
      "name": "Professional",
      "price": 299,
      "currency": "PLN",
      "interval": "month",
      "limits": {
        "contacts": 100000,
        "emailsPerMonth": 500000,
        "users": 20,
        "automations": -1,
        "templates": -1,
        "apiCalls": 500000
      },
      "features": [
        "All Standard features",
        "Unlimited automations",
        "Custom templates",
        "Dedicated support",
        "White label"
      ]
    }
  ]
}
```

---

### 2. Pobierz Obecną Subskrypcję

**Endpoint:** `GET /billing/current-subscription`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "subscription-uuid",
    "planId": "plan-standard",
    "planName": "Standard",
    "status": "active",
    "currentPeriodStart": "2024-11-01T00:00:00Z",
    "currentPeriodEnd": "2024-12-01T00:00:00Z",
    "cancelAtPeriodEnd": false,
    "usage": {
      "contacts": 5432,
      "emailsSent": 23456,
      "users": 3,
      "automations": 5
    },
    "limits": {
      "contacts": 10000,
      "emailsPerMonth": 50000,
      "users": 5,
      "automations": 10
    }
  }
}
```

**Statusy subskrypcji:**
- `active` - aktywna
- `trialing` - trial
- `past_due` - zaległa płatność
- `canceled` - anulowana
- `unpaid` - nieopłacona

---

### 3. Zmień Plan

**Endpoint:** `POST /billing/change-plan/{planId}`

**Request Body:**
```json
{
  "billingInterval": "month"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "subscriptionId": "new-subscription-uuid",
    "planId": "plan-professional",
    "effectiveDate": "2024-12-01T00:00:00Z",
    "proratedAmount": 150.00
  },
  "message": "Plan changed successfully"
}
```

---

### 4. Anuluj Subskrypcję

**Endpoint:** `POST /billing/cancel`

**Request Body:**
```json
{
  "reason": "Too expensive",
  "feedback": "Additional comments..."
}
```

**Response:** `200 OK`

---

### 5. Reaktywuj Subskrypcję

**Endpoint:** `POST /billing/reactivate`

**Response:** `200 OK`

---

### 6. Pobierz Metody Płatności

**Endpoint:** `GET /billing/payment-methods`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "pm-uuid",
      "type": "card",
      "brand": "visa",
      "last4": "4242",
      "expiryMonth": 12,
      "expiryYear": 2025,
      "isDefault": true,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

### 7. Dodaj Metodę Płatności

**Endpoint:** `POST /billing/payment-methods`

**Request Body:**
```json
{
  "type": "card",
  "token": "stripe-token-or-paypal-token",
  "setAsDefault": true
}
```

**Response:** `201 Created`

---

### 8. Usuń Metodę Płatności

**Endpoint:** `DELETE /billing/payment-methods/{id}`

**Response:** `200 OK`

---

### 9. Pobierz Faktury

**Endpoint:** `GET /billing/invoices`

**Query Parameters:**
```
?page=1
&pageSize=25
&status=paid,unpaid
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "invoice-uuid",
      "number": "INV-2024-001",
      "status": "paid",
      "amount": 99.00,
      "currency": "PLN",
      "dueDate": "2024-11-01T00:00:00Z",
      "paidAt": "2024-11-01T10:00:00Z",
      "downloadUrl": "https://example.com/invoices/invoice-uuid.pdf",
      "items": [
        {
          "description": "Standard Plan - November 2024",
          "amount": 99.00,
          "quantity": 1
        }
      ]
    }
  ]
}
```

---

### 10. Pobierz Fakturę

**Endpoint:** `GET /billing/invoices/{id}`

**Response:** `200 OK` - PDF file

---

## 🔑 API KEYS (Klucze API)

### 1. Pobierz Wszystkie Klucze

**Endpoint:** `GET /api-keys`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "key-uuid",
      "name": "Production API Key",
      "key": "ml_live_abc123...",
      "prefix": "ml_live_",
      "permissions": [
        "contacts.read",
        "contacts.write",
        "campaigns.read"
      ],
      "status": "active",
      "lastUsedAt": "2024-11-05T09:30:00Z",
      "createdAt": "2024-01-01T00:00:00Z",
      "expiresAt": null
    }
  ]
}
```

---

### 2. Utwórz Klucz API

**Endpoint:** `POST /api-keys`

**Request Body:**
```json
{
  "name": "New API Key",
  "permissions": [
    "contacts.read",
    "campaigns.read"
  ],
  "expiresAt": "2025-01-01T00:00:00Z"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "key-uuid",
    "name": "New API Key",
    "key": "ml_live_abc123def456...",
    "permissions": ["contacts.read", "campaigns.read"]
  },
  "message": "API key created. Save it now, it won't be shown again!"
}
```

---

### 3. Usuń Klucz API

**Endpoint:** `DELETE /api-keys/{id}`

**Response:** `200 OK`

---

### 4. Regeneruj Klucz API

**Endpoint:** `POST /api-keys/{id}/regenerate`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "key": "ml_live_new_key_here..."
  },
  "message": "API key regenerated. Save it now!"
}
```

---

### 5. Aktywność Klucza API

**Endpoint:** `GET /api-keys/{id}/activities`

**Query Parameters:**
```
?page=1
&pageSize=50
&days=30
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "activity-uuid",
      "endpoint": "/api/v1/contacts",
      "method": "GET",
      "statusCode": 200,
      "ipAddress": "192.168.1.1",
      "userAgent": "MyApp/1.0",
      "timestamp": "2024-11-05T10:30:00Z"
    }
  ]
}
```

---

### 6. Statystyki Użycia API

**Endpoint:** `GET /api-keys/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalCalls": 15432,
    "callsToday": 543,
    "callsThisMonth": 8765,
    "limitPerMonth": 50000,
    "remainingCalls": 41235,
    "topEndpoints": [
      {
        "endpoint": "/api/v1/contacts",
        "calls": 5432
      }
    ]
  }
}
```

---

## 📊 USAGE & ANALYTICS (Użycie i Analityka)

### 1. Statystyki Użycia

**Endpoint:** `GET /usage/statistics`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "contacts": {
      "current": 5432,
      "limit": 10000,
      "percentage": 54.32
    },
    "emailsSent": {
      "current": 23456,
      "limit": 50000,
      "percentage": 46.91
    },
    "users": {
      "current": 3,
      "limit": 5,
      "percentage": 60
    },
    "automations": {
      "current": 5,
      "limit": 10,
      "percentage": 50
    },
    "templates": {
      "current": 25,
      "limit": 50,
      "percentage": 50
    },
    "apiCalls": {
      "current": 15432,
      "limit": 50000,
      "percentage": 30.86
    },
    "storage": {
      "current": 512,
      "limit": 5120,
      "percentage": 10,
      "unit": "MB"
    }
  }
}
```

---

### 2. Historia Użycia

**Endpoint:** `GET /usage/history?days=30`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11-05",
      "contacts": 5432,
      "emailsSent": 543,
      "apiCalls": 876
    },
    {
      "date": "2024-11-04",
      "contacts": 5401,
      "emailsSent": 612,
      "apiCalls": 923
    }
  ]
}
```

---

### 3. Alerty Użycia

**Endpoint:** `GET /usage/alerts`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "alert-uuid",
      "type": "warning",
      "resource": "emails",
      "message": "You've used 90% of your monthly email quota",
      "threshold": 90,
      "currentUsage": 45000,
      "limit": 50000,
      "createdAt": "2024-11-05T10:00:00Z"
    }
  ]
}
```

---

### 4. Dashboard Analytics

**Endpoint:** `GET /analytics/dashboard`

**Query Parameters:**
```
?period=30d
&timezone=Europe/Warsaw
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "overview": {
      "totalContacts": 5432,
      "totalCampaigns": 45,
      "emailsSent": 125432,
      "avgOpenRate": 42.3,
      "avgClickRate": 13.8
    },
    "growth": {
      "contacts": {
        "value": 543,
        "percentage": 11.1,
        "trend": "up"
      },
      "campaigns": {
        "value": 5,
        "percentage": 12.5,
        "trend": "up"
      }
    },
    "recentActivity": [
      {
        "type": "campaign_sent",
        "campaignName": "Newsletter November",
        "timestamp": "2024-11-05T10:00:00Z"
      }
    ],
    "topPerforming": {
      "campaigns": [
        {
          "id": "campaign-uuid",
          "name": "Newsletter",
          "openRate": 58.3,
          "clickRate": 25.1
        }
      ]
    }
  }
}
```

---

### 5. Przegląd Kontaktów

**Endpoint:** `GET /analytics/contacts-overview`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "total": 5432,
    "byStatus": {
      "active": 4987,
      "unsubscribed": 321,
      "bounced": 124
    },
    "bySource": {
      "manual": 2345,
      "import": 2876,
      "api": 211
    },
    "growthChart": [
      {
        "date": "2024-11-01",
        "count": 5401
      },
      {
        "date": "2024-11-05",
        "count": 5432
      }
    ]
  }
}
```

---

### 6. Wydajność Kampanii

**Endpoint:** `GET /analytics/campaign-performance`

**Query Parameters:**
```
?period=30d
&campaignIds=id1,id2
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalSent": 125432,
      "avgOpenRate": 42.3,
      "avgClickRate": 13.8,
      "avgBounceRate": 1.2,
      "avgUnsubscribeRate": 0.4
    },
    "campaigns": [
      {
        "id": "campaign-uuid",
        "name": "Newsletter November",
        "sent": 2543,
        "openRate": 45.2,
        "clickRate": 15.3
      }
    ]
  }
}
```

---

## 📡 WEBHOOKS

### 1. Pobierz Wszystkie Webhooki

**Endpoint:** `GET /webhooks`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "webhook-uuid",
      "url": "https://example.com/webhook",
      "events": [
        "contact.created",
        "campaign.sent",
        "email.opened"
      ],
      "status": "active",
      "secret": "whsec_abc123...",
      "createdAt": "2024-01-01T00:00:00Z",
      "lastTriggeredAt": "2024-11-05T10:00:00Z"
    }
  ]
}
```

---

### 2. Utwórz Webhook

**Endpoint:** `POST /webhooks`

**Request Body:**
```json
{
  "url": "https://example.com/webhook",
  "events": [
    "contact.created",
    "contact.updated",
    "campaign.sent"
  ]
}
```

**Response:** `201 Created`

**Dostępne eventy:**
- `contact.created`, `contact.updated`, `contact.deleted`
- `campaign.sent`, `campaign.scheduled`, `campaign.completed`
- `email.opened`, `email.clicked`, `email.bounced`
- `list.subscribed`, `list.unsubscribed`
- `automation.triggered`, `automation.completed`

---

### 3. Usuń Webhook

**Endpoint:** `DELETE /webhooks/{id}`

**Response:** `200 OK`

---

### 4. Logi Webhooka

**Endpoint:** `GET /webhooks/{id}/logs`

**Query Parameters:**
```
?page=1
&pageSize=50
&status=success,failed
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "log-uuid",
      "event": "contact.created",
      "payload": {
        "contactId": "contact-uuid"
      },
      "statusCode": 200,
      "response": "OK",
      "attempts": 1,
      "timestamp": "2024-11-05T10:00:00Z"
    }
  ]
}
```

---

## 📂 FILES (Pliki)

### 1. Upload Pliku

**Endpoint:** `POST /files/upload`

**Content-Type:** `multipart/form-data`

**Form Data:**
```
file: (file)
type: "avatar" | "attachment" | "import"
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "file-uuid",
    "filename": "document.pdf",
    "url": "https://example.com/files/file-uuid.pdf",
    "size": 1024567,
    "mimeType": "application/pdf",
    "uploadedAt": "2024-11-05T10:00:00Z"
  }
}
```

---

### 2. Pobierz Plik

**Endpoint:** `GET /files/{id}`

**Response:** File download

---

## 📧 TRACKING (Śledzenie)

### 1. Śledzenie Otwarć

**Endpoint:** `POST /tracking/open`

**Request Body:**
```json
{
  "campaignId": "campaign-uuid",
  "contactId": "contact-uuid",
  "timestamp": "2024-11-05T10:15:00Z",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1"
}
```

**Response:** `200 OK`

---

### 2. Śledzenie Kliknięć

**Endpoint:** `POST /tracking/click`

**Request Body:**
```json
{
  "campaignId": "campaign-uuid",
  "contactId": "contact-uuid",
  "url": "https://example.com/product",
  "timestamp": "2024-11-05T10:20:00Z",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1"
}
```

**Response:** `200 OK`

---

## 🔌 INTEGRATIONS (Integracje)

### 1. Dostępne Integracje

**Endpoint:** `GET /integrations/available`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "zapier",
      "name": "Zapier",
      "description": "Connect with 3000+ apps",
      "logo": "https://example.com/logos/zapier.png",
      "category": "automation"
    },
    {
      "id": "stripe",
      "name": "Stripe",
      "description": "Payment processing",
      "logo": "https://example.com/logos/stripe.png",
      "category": "payment"
    }
  ]
}
```

---

### 2. Połącz Integrację

**Endpoint:** `POST /integrations/{type}/connect`

**Request Body:**
```json
{
  "credentials": {
    "apiKey": "integration-api-key",
    "apiSecret": "integration-secret"
  },
  "config": {
    "webhookUrl": "https://example.com/webhook"
  }
}
```

**Response:** `200 OK`

---

### 3. Połączone Integracje

**Endpoint:** `GET /integrations/connected`

**Response:** `200 OK`

---

### 4. Rozłącz Integrację

**Endpoint:** `DELETE /integrations/{id}`

**Response:** `200 OK`

---

## Kody Błędów

### HTTP Status Codes

- `200 OK` - Żądanie zakończone sukcesem
- `201 Created` - Zasób utworzony pomyślnie
- `400 Bad Request` - Nieprawidłowe żądanie
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień
- `404 Not Found` - Zasób nie znaleziony
- `422 Unprocessable Entity` - Błąd walidacji
- `429 Too Many Requests` - Przekroczono limit żądań
- `500 Internal Server Error` - Błąd serwera
- `503 Service Unavailable` - Serwis niedostępny

### Format Błędu

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": ["Email is required", "Email must be valid"],
    "password": ["Password must be at least 8 characters"]
  },
  "timestamp": "2024-11-05T10:00:00Z"
}
```

---

## Modele Danych

### Contact (Kontakt)

```typescript
interface Contact {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  organization?: string;
  tags: string[];
  customFields: CustomField[];
  lists: string[];
  status: 'active' | 'inactive' | 'bounced' | 'unsubscribed';
  subscriptionStatus: 'subscribed' | 'unsubscribed' | 'pending';
  createdAt: Date;
  updatedAt: Date;
  lastActivity?: Date;
  engagementScore: number;
  location?: {
    country?: string;
    state?: string;
    city?: string;
    timezone?: string;
  };
}
```

### CustomField (Pole Niestandardowe)

```typescript
interface CustomField {
  id: string;
  name: string;
  value: any;
  type: 'text' | 'number' | 'date' | 'dropdown' | 'checkbox';
}
```

### ContactList (Lista Kontaktów)

```typescript
interface ContactList {
  id: string;
  name: string;
  description?: string;
  type: 'standard' | 'smart';
  subscriberCount: number;
  isSmartList: boolean;
  conditions?: SmartListCondition[];
  createdAt: Date;
  updatedAt: Date;
}
```

### SmartListCondition (Warunek Inteligentnej Listy)

```typescript
interface SmartListCondition {
  field: string;
  operator: 'equals' | 'not_equals' | 'contains' | 'greater' | 'less';
  value: any;
}
```

### Campaign (Kampania)

```typescript
interface Campaign {
  id: string;
  name: string;
  subject: string;
  previewText?: string;
  fromName: string;
  fromEmail: string;
  replyTo?: string;
  status: 'draft' | 'scheduled' | 'sending' | 'sent' | 'paused' | 'cancelled';
  type: 'regular' | 'automated' | 'abtest';
  content: {
    html: string;
    text: string;
  };
  recipients: {
    lists: string[];
    segments: string[];
    excludeLists: string[];
    totalCount: number;
  };
  schedule?: {
    sendAt: Date;
    timezone: string;
  };
  statistics: CampaignStatistics;
  abTest?: ABTest;
  createdAt: Date;
  updatedAt: Date;
  sentAt?: Date;
}
```

### CampaignStatistics (Statystyki Kampanii)

```typescript
interface CampaignStatistics {
  sent: number;
  delivered: number;
  opens: number;
  uniqueOpens: number;
  clicks: number;
  uniqueClicks: number;
  bounces: number;
  complaints: number;
  unsubscribes: number;
  performance: {
    openRate: number;
    clickRate: number;
    bounceRate: number;
    unsubscribeRate: number;
  };
}
```

### Template (Szablon)

```typescript
interface Template {
  id: string;
  name: string;
  description?: string;
  category: 'promotional' | 'newsletter' | 'welcome' | 'transactional' | 'announcement' | 'event' | 'survey';
  thumbnail?: string;
  content: {
    html: string;
    text: string;
  };
  variables: TemplateVariable[];
  isArchived: boolean;
  usageCount: number;
  createdAt: Date;
  updatedAt: Date;
}
```

### TemplateVariable (Zmienna Szablonu)

```typescript
interface TemplateVariable {
  name: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'image';
  required: boolean;
  defaultValue?: any;
}
```

### Automation (Automatyzacja)

```typescript
interface Automation {
  id: string;
  name: string;
  description?: string;
  status: 'active' | 'paused' | 'draft';
  trigger: AutomationTrigger;
  actions: AutomationAction[];
  statistics: AutomationStatistics;
  createdAt: Date;
  updatedAt: Date;
  lastRunAt?: Date;
}
```

### AutomationTrigger (Wyzwalacz Automatyzacji)

```typescript
interface AutomationTrigger {
  type: 'contact_subscribed' | 'email_opened' | 'link_clicked' | 'date_reached' | 'custom_field_changed' | 'tag_added';
  config: Record<string, any>;
}
```

### AutomationAction (Akcja Automatyzacji)

```typescript
interface AutomationAction {
  id: string;
  type: 'send_email' | 'add_tag' | 'remove_tag' | 'wait' | 'webhook' | 'send_sms' | 'update_field';
  delay: {
    value: number;
    unit: 'minutes' | 'hours' | 'days' | 'weeks';
  };
  config: Record<string, any>;
}
```

### User (Użytkownik)

```typescript
interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'admin' | 'user';
  avatar?: string;
  phone?: string;
  company?: string;
  timezone: string;
  language: string;
  preferences: UserPreferences;
  createdAt: Date;
}
```

### UserPreferences (Preferencje Użytkownika)

```typescript
interface UserPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  weeklyReport: boolean;
}
```

---

## Przykłady Integracji

### Przykład 1: Tworzenie Kontaktu i Wysyłanie Kampanii

```typescript
// 1. Login
const loginResponse = await fetch('https://api.mailist.com/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password'
  })
});
const { data: { token } } = await loginResponse.json();

// 2. Utwórz kontakt
const contactResponse = await fetch('https://api.mailist.com/api/v1/contacts', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    email: 'nowy@example.com',
    firstName: 'Jan',
    lastName: 'Kowalski',
    tags: ['nowy'],
    lists: ['list-id']
  })
});

// 3. Utwórz kampanię
const campaignResponse = await fetch('https://api.mailist.com/api/v1/campaigns', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: 'Welcome Campaign',
    subject: 'Witaj!',
    fromName: 'Mailist',
    fromEmail: 'hello@mailist.com',
    content: {
      html: '<h1>Witaj {{firstName}}!</h1>',
      text: 'Witaj {{firstName}}!'
    },
    recipients: {
      lists: ['list-id']
    }
  })
});

// 4. Wyślij kampanię
const { data: { id: campaignId } } = await campaignResponse.json();
await fetch(`https://api.mailist.com/api/v1/campaigns/${campaignId}/send`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

### Przykład 2: Import Kontaktów z CSV

```typescript
const formData = new FormData();
formData.append('file', csvFile);
formData.append('mapping', JSON.stringify({
  email: 'Email',
  firstName: 'First Name',
  lastName: 'Last Name'
}));
formData.append('skipDuplicates', 'true');

const response = await fetch('https://api.mailist.com/api/v1/lists/list-id/import', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const result = await response.json();
console.log(`Imported: ${result.data.imported}, Skipped: ${result.data.skipped}`);
```

---

### Przykład 3: Webhook Handler

```javascript
// Node.js Express przykład
app.post('/webhook', (req, res) => {
  const event = req.body;

  switch(event.type) {
    case 'email.opened':
      console.log(`Email opened by ${event.data.contactId}`);
      break;
    case 'email.clicked':
      console.log(`Link clicked: ${event.data.url}`);
      break;
    case 'contact.created':
      console.log(`New contact: ${event.data.email}`);
      break;
  }

  res.status(200).send('OK');
});
```

---

## Uwagi Końcowe

### Rate Limiting

API stosuje limit żądań:
- **Free Plan:** 100 żądań/minutę
- **Standard Plan:** 500 żądań/minutę
- **Professional Plan:** 2000 żądań/minutę

Nagłówki odpowiedzi:
```
X-RateLimit-Limit: 500
X-RateLimit-Remaining: 450
X-RateLimit-Reset: 1699185600
```

### Wersjonowanie

API używa wersjonowania w URL (`/api/v1/`). Przy wprowadzaniu breaking changes zostanie stworzona nowa wersja.

### Paginacja

Domyślna wielkość strony: 25 elementów
Maksymalna wielkość strony: 100 elementów

### Daty i Czas

Wszystkie daty są w formacie ISO 8601 UTC:
```
2024-11-05T10:00:00Z
```

### Bezpieczeństwo

- Wszystkie połączenia muszą używać HTTPS
- Tokeny JWT wygasają po 24 godzinach
- Refresh tokeny wygasają po 30 dniach
- Hasła muszą być hashowane (bcrypt, Argon2)

---

## Kontakt i Wsparcie

W razie pytań lub problemów z integracją:
- Email: dev@mailist.com
- Dokumentacja: https://docs.mailist.com
- GitHub: https://github.com/mailist-com/mailist-api

---

**Wersja dokumentacji:** 1.0.0
**Data ostatniej aktualizacji:** 2024-11-05
**Autor:** Claude (AI Assistant for Mailist Development Team)
