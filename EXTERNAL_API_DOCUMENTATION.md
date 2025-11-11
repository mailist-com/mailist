# External REST API - Dokumentacja

## Przegląd

Implementacja zewnętrznego REST API umożliwia integrację z systemem Mailist z wykorzystaniem kluczy API. API jest całkowicie odseparowane od głównego API aplikacji i posiada własną autoryzację oraz system bezpieczeństwa.

## Architektura

### Pakiety

Cała funkcjonalność zewnętrznego API znajduje się w dedykowanym pakiecie:
```
com.mailist.mailist.externalapi/
├── controller/          # Kontrolery REST API
├── service/            # Logika biznesowa
├── dto/               # Data Transfer Objects
├── event/             # Eventy domenowe
└── listener/          # Event Listenery dla notyfikacji
```

### Komponenty

1. **ApiKeyAuthenticationFilter** - Filtr Spring Security do autoryzacji kluczy API
2. **ApiKeyAuthenticationToken** - Custom token autoryzacji
3. **ExternalApiService** - Serwis główny logiki biznesowej
4. **ExternalContactController** - Kontroler dla zarządzania kontaktami
5. **ExternalApiEventListener** - Listener do tworzenia notyfikacji

## Autoryzacja

### Konfiguracja Security

Zewnętrzne API jest zabezpieczone osobnym filtrem autoryzacji (`ApiKeyAuthenticationFilter`), który:
- Działa tylko dla endpointów `/api/v1/external/**`
- Autoryzuje za pomocą kluczy API (zamiast JWT)
- Sprawdza uprawnienia klucza API
- Rejestruje użycie klucza (IP, czas, licznik wywołań)

### Obsługiwane nagłówki

API akceptuje klucze API w dwóch formatach:

1. **X-API-Key header** (preferowany):
```
X-API-Key: ml_live_abc123...
```

2. **Authorization Bearer header**:
```
Authorization: Bearer ml_live_abc123...
```

### Bezpieczeństwo

- Klucze API są hashowane SHA-256 przed zapisem do bazy danych
- Każde użycie klucza jest logowane (IP, timestamp)
- Klucze mogą wygasać (opcjonalne pole `expiresAt`)
- Klucze mogą być aktywowane/deaktywowane
- System uprawnień per klucz API (np. `contacts.read`, `contacts.write`)

## Endpointy API

### 1. Dodaj nowy kontakt

**POST** `/api/v1/external/contacts`

Tworzy nowy kontakt i opcjonalnie dodaje go do listy mailingowej.

**Wymagane uprawnienie:** `contacts.write`

**Request Body:**
```json
{
  "first_name": "Jan",
  "last_name": "Kowalski",
  "email": "jan.kowalski@example.com",
  "phone": "+48123456789",
  "tags": ["vip", "newsletter"],
  "list_id": 123,
  "list_name": "Newsletter Subscribers"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Contact created successfully",
  "data": {
    "id": 456,
    "first_name": "Jan",
    "last_name": "Kowalski",
    "email": "jan.kowalski@example.com",
    "phone": "+48123456789",
    "tags": ["vip", "newsletter"],
    "lead_score": 0,
    "created_at": "2025-11-11T10:30:00",
    "updated_at": "2025-11-11T10:30:00"
  }
}
```

### 2. Aktualizuj kontakt

**PUT** `/api/v1/external/contacts/{email}`

Aktualizuje istniejący kontakt.

**Wymagane uprawnienie:** `contacts.write`

**Request Body:**
```json
{
  "first_name": "Jan",
  "last_name": "Nowak",
  "phone": "+48987654321",
  "tags": ["vip", "premium"]
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Contact updated successfully",
  "data": {
    "id": 456,
    "first_name": "Jan",
    "last_name": "Nowak",
    "email": "jan.kowalski@example.com",
    "phone": "+48987654321",
    "tags": ["vip", "premium"],
    "lead_score": 0,
    "created_at": "2025-11-11T10:30:00",
    "updated_at": "2025-11-11T10:35:00"
  }
}
```

### 3. Pobierz kontakt

**GET** `/api/v1/external/contacts/{email}`

Pobiera dane kontaktu po adresie email.

**Wymagane uprawnienie:** `contacts.read`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "first_name": "Jan",
    "last_name": "Nowak",
    "email": "jan.kowalski@example.com",
    "phone": "+48987654321",
    "tags": ["vip", "premium"],
    "lead_score": 0,
    "created_at": "2025-11-11T10:30:00",
    "updated_at": "2025-11-11T10:35:00"
  }
}
```

### 4. Dodaj kontakt do listy

**POST** `/api/v1/external/contacts/{contactId}/add-to-list`

Dodaje kontakt do listy mailingowej po ID listy lub nazwie.

**Wymagane uprawnienie:** `contacts.write`

**Query Parameters:**
- `listId` (opcjonalny) - ID listy
- `listName` (opcjonalny) - Nazwa listy (zostanie utworzona jeśli nie istnieje)

**Przykład:**
```
POST /api/v1/external/contacts/456/add-to-list?listName=Newsletter
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Contact added to list successfully",
  "data": {
    "id": 456,
    "first_name": "Jan",
    "last_name": "Nowak",
    "email": "jan.kowalski@example.com",
    ...
  }
}
```

## Uprawnienia

System wspiera granularne uprawnienia dla kluczy API:

| Uprawnienie | Opis |
|-------------|------|
| `contacts.read` | Odczyt kontaktów |
| `contacts.write` | Tworzenie i aktualizacja kontaktów |
| `contacts.delete` | Usuwanie kontaktów |
| `lists.read` | Odczyt list mailingowych |
| `lists.write` | Tworzenie i aktualizacja list |
| `*` | Pełny dostęp (admin) |

System wspiera również uprawnienia wildcard:
- `contacts.*` - wszystkie uprawnienia dla kontaktów
- `lists.*` - wszystkie uprawnienia dla list

## Eventy i Notyfikacje

System automatycznie tworzy notyfikacje dla użytkownika, który stworzył klucz API, gdy:

### 1. Dodanie kontaktu (`ExternalApiContactAddedEvent`)

**Wyzwalane gdy:** Nowy kontakt zostanie dodany przez zewnętrzne API

**Notyfikacja zawiera:**
- Email kontaktu
- Klucz API, który wykonał operację
- Nazwa listy (jeśli kontakt został dodany do listy)
- Link do podglądu kontaktu

**Przykład notyfikacji:**
```
Tytuł: "Nowy kontakt z API"
Wiadomość: "Kontakt jan.kowalski@example.com został dodany przez zewnętrzny
           system (API Key: ml_live_...1234) do listy Newsletter"
```

### 2. Aktualizacja kontaktu (`ExternalApiContactUpdatedEvent`)

**Wyzwalane gdy:** Istniejący kontakt zostanie zaktualizowany przez zewnętrzne API

**Notyfikacja zawiera:**
- Email kontaktu
- Klucz API, który wykonał operację
- Link do podglądu kontaktu

**Przykład notyfikacji:**
```
Tytuł: "Kontakt zaktualizowany przez API"
Wiadomość: "Kontakt jan.kowalski@example.com został zaktualizowany przez
           zewnętrzny system (API Key: ml_live_...1234)"
```

### Event Listener

`ExternalApiEventListener` obsługuje eventy asynchronicznie i tworzy notyfikacje poprzez `NotificationService`.

## Obsługa błędów

API zwraca standardowe odpowiedzi błędów:

### 400 Bad Request
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Contact with this email already exists"
  }
}
```

### 403 Forbidden
```json
{
  "success": false,
  "error": {
    "code": "PERMISSION_DENIED",
    "message": "API key does not have permission: contacts.write"
  }
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": {
    "code": "CONTACT_NOT_FOUND",
    "message": "Contact not found: jan.kowalski@example.com"
  }
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "Internal server error"
  }
}
```

## Przykłady użycia

### cURL

```bash
# Dodaj nowy kontakt
curl -X POST https://api.mailist.com/api/v1/external/contacts \
  -H "X-API-Key: ml_live_your_api_key_here" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Jan",
    "last_name": "Kowalski",
    "email": "jan.kowalski@example.com",
    "list_name": "Newsletter"
  }'

# Pobierz kontakt
curl -X GET https://api.mailist.com/api/v1/external/contacts/jan.kowalski@example.com \
  -H "X-API-Key: ml_live_your_api_key_here"

# Aktualizuj kontakt
curl -X PUT https://api.mailist.com/api/v1/external/contacts/jan.kowalski@example.com \
  -H "X-API-Key: ml_live_your_api_key_here" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+48123456789",
    "tags": ["vip"]
  }'
```

### JavaScript (fetch)

```javascript
const apiKey = 'ml_live_your_api_key_here';
const apiUrl = 'https://api.mailist.com/api/v1/external';

// Dodaj kontakt
async function addContact(contactData) {
  const response = await fetch(`${apiUrl}/contacts`, {
    method: 'POST',
    headers: {
      'X-API-Key': apiKey,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(contactData)
  });

  return await response.json();
}

// Użycie
const result = await addContact({
  first_name: 'Jan',
  last_name: 'Kowalski',
  email: 'jan.kowalski@example.com',
  list_name: 'Newsletter'
});

console.log(result);
```

### Python (requests)

```python
import requests

api_key = 'ml_live_your_api_key_here'
api_url = 'https://api.mailist.com/api/v1/external'

headers = {
    'X-API-Key': api_key,
    'Content-Type': 'application/json'
}

# Dodaj kontakt
contact_data = {
    'first_name': 'Jan',
    'last_name': 'Kowalski',
    'email': 'jan.kowalski@example.com',
    'list_name': 'Newsletter'
}

response = requests.post(
    f'{api_url}/contacts',
    headers=headers,
    json=contact_data
)

print(response.json())
```

## Integracja z Landing Page

Przykład formularza landing page z integracją API:

```html
<form id="newsletter-form">
  <input type="text" name="first_name" placeholder="Imię" required>
  <input type="text" name="last_name" placeholder="Nazwisko" required>
  <input type="email" name="email" placeholder="Email" required>
  <button type="submit">Zapisz się</button>
</form>

<script>
document.getElementById('newsletter-form').addEventListener('submit', async (e) => {
  e.preventDefault();

  const formData = new FormData(e.target);
  const data = {
    first_name: formData.get('first_name'),
    last_name: formData.get('last_name'),
    email: formData.get('email'),
    list_name: 'Landing Page Newsletter',
    tags: ['landing-page']
  };

  try {
    const response = await fetch('https://api.mailist.com/api/v1/external/contacts', {
      method: 'POST',
      headers: {
        'X-API-Key': 'ml_live_your_api_key_here',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const result = await response.json();

    if (result.success) {
      alert('Dziękujemy za zapisanie się!');
      e.target.reset();
    } else {
      alert('Wystąpił błąd: ' + result.error.message);
    }
  } catch (error) {
    alert('Wystąpił błąd połączenia');
  }
});
</script>
```

## Monitoring i Statystyki

System automatycznie rejestruje:
- Liczbę wywołań API per klucz
- Ostatni czas użycia klucza
- Adres IP ostatniego użycia
- Historię aktywności (tabela `api_key_activities`)

Te informacje są dostępne w panelu administracyjnym w sekcji zarządzania kluczami API.

## Limity

Obecnie brak limitów rate-limiting, ale można je łatwo dodać używając biblioteki Bucket4j lub podobnej.

## Rozwój

Planowane rozszerzenia:
- Dodatkowe endpointy dla kampanii
- Webhook'i dla eventów
- Rate limiting
- API key scopes (ograniczenie dostępu do określonych zasobów)
- Bulk operations (dodawanie wielu kontaktów jednocześnie)
