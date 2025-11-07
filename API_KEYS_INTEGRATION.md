# API Keys & Integration - Dokumentacja

## Przegląd

System zarządzania API keys pozwala użytkownikom Mailist na integrację swoich aplikacji poprzez REST API. Każdy klucz API może mieć przypisane różne uprawnienia i jest śledzony pod kątem użycia.

## Endpointy API

### 1. Pobierz wszystkie klucze API

```http
GET /api/v1/api-keys
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "key-uuid",
      "name": "Production API Key",
      "description": "Key for production environment",
      "key": "ml_live_...abc123",
      "prefix": "ml_live_",
      "status": "ACTIVE",
      "permissions": ["contacts.read", "contacts.write", "campaigns.read"],
      "lastUsedAt": "2024-11-06T10:30:00",
      "lastUsedIpAddress": "192.168.1.1",
      "totalCalls": 1543,
      "expiresAt": null,
      "createdAt": "2024-11-01T00:00:00",
      "updatedAt": "2024-11-06T10:30:00"
    }
  ]
}
```

---

### 2. Utwórz nowy klucz API

```http
POST /api/v1/api-keys
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "My Application Key",
  "description": "API key for my mobile app",
  "permissions": [
    "contacts.read",
    "contacts.write",
    "lists.read"
  ],
  "expiresAt": "2025-12-31T23:59:59"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "apiKey": {
      "id": "new-key-uuid",
      "name": "My Application Key",
      "key": "ml_live_...xyz789",
      "status": "ACTIVE",
      "permissions": ["contacts.read", "contacts.write", "lists.read"],
      "createdAt": "2024-11-06T11:00:00"
    },
    "plainKey": "ml_live_AbCdEfGhIjKlMnOpQrStUvWxYz123456789",
    "message": "API key created successfully. Save it now, it won't be shown again!"
  },
  "message": "API key created successfully"
}
```

**⚠️ WAŻNE:** Klucz w polu `plainKey` jest pokazywany tylko RAZ podczas tworzenia. Zapisz go w bezpiecznym miejscu!

---

### 3. Usuń klucz API

```http
DELETE /api/v1/api-keys/{id}
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "API key deleted successfully"
}
```

---

### 4. Odwołaj klucz API

```http
POST /api/v1/api-keys/{id}/revoke
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "API key revoked successfully"
}
```

---

### 5. Pobierz aktywność klucza API

```http
GET /api/v1/api-keys/{id}/activities?page=0&pageSize=50
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "activities": [
      {
        "id": "activity-uuid",
        "apiKeyId": "key-uuid",
        "endpoint": "/api/v1/contacts",
        "method": "GET",
        "statusCode": 200,
        "responseTime": 145,
        "ipAddress": "192.168.1.1",
        "userAgent": "MyApp/1.0",
        "errorMessage": null,
        "timestamp": "2024-11-06T10:30:00"
      }
    ],
    "pagination": {
      "page": 0,
      "pageSize": 50,
      "total": 1543,
      "totalPages": 31,
      "hasNextPage": true,
      "hasPreviousPage": false
    }
  }
}
```

---

### 6. Pobierz statystyki użycia

```http
GET /api/v1/api-keys/statistics
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalKeys": 3,
    "activeKeys": 2,
    "totalCalls": 15432,
    "topEndpoints": {
      "/api/v1/contacts": 8543,
      "/api/v1/campaigns": 3214,
      "/api/v1/lists": 2187
    }
  }
}
```

---

### 7. Pobierz dostępne uprawnienia

```http
GET /api/v1/api-keys/permissions
Authorization: Bearer {jwt_token}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "permission": "contacts.read",
      "description": "Read contacts"
    },
    {
      "permission": "contacts.write",
      "description": "Create and update contacts"
    },
    {
      "permission": "contacts.delete",
      "description": "Delete contacts"
    },
    {
      "permission": "campaigns.read",
      "description": "Read campaigns"
    },
    {
      "permission": "campaigns.write",
      "description": "Create and update campaigns"
    },
    {
      "permission": "campaigns.send",
      "description": "Send campaigns"
    },
    {
      "permission": "*",
      "description": "Full access (admin)"
    }
  ]
}
```

---

## Używanie API Keys

### Autentykacja z API Key

Wszystkie żądania do API muszą zawierać klucz API w nagłówku:

```http
X-API-Key: ml_live_YourApiKeyHere
```

### Przykład użycia (cURL)

```bash
# Pobierz listę kontaktów używając API key
curl -X GET "https://api.mailist.com/api/v1/contacts" \
  -H "X-API-Key: ml_live_YourApiKeyHere" \
  -H "Content-Type: application/json"
```

### Przykład użycia (JavaScript)

```javascript
const API_KEY = 'ml_live_YourApiKeyHere';
const API_URL = 'https://api.mailist.com/api/v1';

async function getContacts() {
  const response = await fetch(`${API_URL}/contacts`, {
    method: 'GET',
    headers: {
      'X-API-Key': API_KEY,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const data = await response.json();
  return data.data;
}

// Użycie
getContacts()
  .then(contacts => console.log('Contacts:', contacts))
  .catch(error => console.error('Error:', error));
```

### Przykład użycia (Python)

```python
import requests

API_KEY = 'ml_live_YourApiKeyHere'
API_URL = 'https://api.mailist.com/api/v1'

def get_contacts():
    headers = {
        'X-API-Key': API_KEY,
        'Content-Type': 'application/json'
    }

    response = requests.get(f'{API_URL}/contacts', headers=headers)
    response.raise_for_status()

    return response.json()['data']

# Użycie
try:
    contacts = get_contacts()
    print(f'Found {len(contacts)} contacts')
except requests.exceptions.RequestException as e:
    print(f'Error: {e}')
```

### Przykład użycia (PHP)

```php
<?php

$apiKey = 'ml_live_YourApiKeyHere';
$apiUrl = 'https://api.mailist.com/api/v1';

function getContacts($apiKey, $apiUrl) {
    $ch = curl_init($apiUrl . '/contacts');

    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'X-API-Key: ' . $apiKey,
        'Content-Type: application/json'
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode !== 200) {
        throw new Exception("HTTP Error: " . $httpCode);
    }

    $data = json_decode($response, true);
    return $data['data'];
}

// Użycie
try {
    $contacts = getContacts($apiKey, $apiUrl);
    echo "Found " . count($contacts) . " contacts\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
```

---

## Tworzenie kontaktu przez API

```bash
curl -X POST "https://api.mailist.com/api/v1/contacts" \
  -H "X-API-Key: ml_live_YourApiKeyHere" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "tags": ["api", "new-customer"],
    "lists": ["list-uuid"],
    "status": "active",
    "subscriptionStatus": "subscribed"
  }'
```

---

## Wysyłanie kampanii przez API

```bash
# 1. Utwórz kampanię
curl -X POST "https://api.mailist.com/api/v1/campaigns" \
  -H "X-API-Key: ml_live_YourApiKeyHere" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Welcome Campaign",
    "subject": "Welcome to Mailist!",
    "fromName": "Mailist Team",
    "fromEmail": "hello@mailist.com",
    "content": {
      "html": "<h1>Welcome {{firstName}}!</h1>",
      "text": "Welcome {{firstName}}!"
    },
    "recipients": {
      "lists": ["list-uuid"]
    }
  }'

# 2. Wyślij kampanię
curl -X POST "https://api.mailist.com/api/v1/campaigns/{campaign-id}/send" \
  -H "X-API-Key: ml_live_YourApiKeyHere" \
  -H "Content-Type: application/json"
```

---

## Bezpieczeństwo

### Najlepsze praktyki:

1. **NIE umieszczaj kluczy API w kodzie źródłowym**
   - Użyj zmiennych środowiskowych
   - Użyj menedżera sekretów (AWS Secrets Manager, Azure Key Vault, etc.)

2. **Ogranicz uprawnienia**
   - Nadawaj tylko niezbędne uprawnienia
   - Używaj różnych kluczy dla różnych środowisk (dev, staging, production)

3. **Rotacja kluczy**
   - Regularnie odwołuj i twórz nowe klucze
   - Ustawiaj datę wygaśnięcia dla kluczy

4. **Monitoruj użycie**
   - Regularnie sprawdzaj logi aktywności
   - Ustawiaj alerty dla podejrzanej aktywności

5. **HTTPS zawsze**
   - Nigdy nie wysyłaj kluczy API przez niezabezpieczone połączenie

---

## Przykładowa aplikacja Node.js

```javascript
// api-client.js
const axios = require('axios');

class MailistClient {
  constructor(apiKey, baseUrl = 'https://api.mailist.com/api/v1') {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;

    this.client = axios.create({
      baseURL: this.baseUrl,
      headers: {
        'X-API-Key': this.apiKey,
        'Content-Type': 'application/json'
      }
    });
  }

  // Contacts
  async getContacts(params = {}) {
    const response = await this.client.get('/contacts', { params });
    return response.data.data;
  }

  async createContact(contact) {
    const response = await this.client.post('/contacts', contact);
    return response.data.data;
  }

  async updateContact(id, updates) {
    const response = await this.client.put(`/contacts/${id}`, updates);
    return response.data.data;
  }

  async deleteContact(id) {
    await this.client.delete(`/contacts/${id}`);
  }

  // Campaigns
  async getCampaigns(params = {}) {
    const response = await this.client.get('/campaigns', { params });
    return response.data.data;
  }

  async createCampaign(campaign) {
    const response = await this.client.post('/campaigns', campaign);
    return response.data.data;
  }

  async sendCampaign(id) {
    const response = await this.client.post(`/campaigns/${id}/send`);
    return response.data.data;
  }
}

// Użycie
const client = new MailistClient(process.env.MAILIST_API_KEY);

async function main() {
  try {
    // Pobierz kontakty
    const contacts = await client.getContacts({ page: 0, pageSize: 25 });
    console.log('Contacts:', contacts);

    // Utwórz nowy kontakt
    const newContact = await client.createContact({
      email: 'john@example.com',
      firstName: 'John',
      lastName: 'Doe',
      tags: ['api-created']
    });
    console.log('Created contact:', newContact);

    // Utwórz i wyślij kampanię
    const campaign = await client.createCampaign({
      name: 'Welcome Campaign',
      subject: 'Welcome!',
      fromName: 'Team',
      fromEmail: 'hello@example.com',
      content: {
        html: '<h1>Welcome {{firstName}}!</h1>'
      },
      recipients: {
        lists: ['list-id']
      }
    });

    await client.sendCampaign(campaign.id);
    console.log('Campaign sent!');

  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
  }
}

main();
```

---

## Rate Limiting

API stosuje limity zapytań:

- **Free Plan:** 100 żądań/minutę
- **Standard Plan:** 500 żądań/minutę
- **Professional Plan:** 2000 żądań/minutę

Nagłówki odpowiedzi:
```
X-RateLimit-Limit: 500
X-RateLimit-Remaining: 450
X-RateLimit-Reset: 1699185600
```

---

## Kody błędów

- `401 Unauthorized` - Brak lub nieprawidłowy klucz API
- `403 Forbidden` - Brak uprawnień do wykonania operacji
- `404 Not Found` - Zasób nie istnieje
- `422 Unprocessable Entity` - Błąd walidacji
- `429 Too Many Requests` - Przekroczono limit żądań
- `500 Internal Server Error` - Błąd serwera

---

## Wsparcie

W razie pytań:
- Email: api-support@mailist.com
- Dokumentacja: https://docs.mailist.com
- GitHub: https://github.com/mailist-com/mailist-api
