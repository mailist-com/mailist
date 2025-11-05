# Email Configuration Guide

## Przegląd

Aplikacja Mailist obsługuje wysyłkę emaili transakcyjnych (kody weryfikacyjne, resetowanie hasła, etc.) przez SMTP. Można korzystać z różnych dostawców:

- **Gmail** (rekomendowane dla development/testów)
- **OVH Mail** (rekomendowane dla produkcji)
- **EmailLabs API** (alternatywa)

## Konfiguracja Gmail (Development)

### Krok 1: Włącz weryfikację dwuetapową

1. Przejdź do https://myaccount.google.com/security
2. W sekcji "Signing in to Google" włącz **2-Step Verification**
3. Postępuj zgodnie z instrukcjami na ekranie

### Krok 2: Wygeneruj hasło aplikacji

1. Po włączeniu weryfikacji dwuetapowej, przejdź do https://myaccount.google.com/apppasswords
2. W polu "Select app" wybierz **Mail**
3. W polu "Select device" wybierz **Other (Custom name)**
4. Wpisz nazwę: **Mailist Development**
5. Kliknij **Generate**
6. Skopiuj 16-znakowe hasło (bez spacji)

### Krok 3: Ustaw zmienne środowiskowe

Stwórz plik `.env` w głównym katalogu projektu (na podstawie `.env.example`):

```bash
# Email Configuration (Gmail)
MAIL_USERNAME=twoj-email@gmail.com
MAIL_PASSWORD=abcd efgh ijkl mnop  # 16-znakowe hasło aplikacji
MAIL_FROM_ADDRESS=noreply@mailist.com
MAIL_FROM_NAME=Mailist
```

### Krok 4: Załaduj zmienne środowiskowe

**Linux/macOS:**
```bash
export $(cat .env | xargs)
```

**Windows (PowerShell):**
```powershell
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Content env:\$name $value
}
```

**Alternatywnie:** Możesz użyć narzędzi jak `dotenv` lub skonfigurować zmienne w IDE (IntelliJ IDEA, VS Code).

### Krok 5: Uruchom aplikację

```bash
./mvnw spring-boot:run
```

### Testowanie

Po uruchomieniu aplikacji, zarejestruj nowego użytkownika:

```bash
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "email": "test@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "acceptTerms": true
}
```

Powinieneś otrzymać email z 6-cyfrowym kodem weryfikacyjnym.

---

## Konfiguracja OVH Mail (Production)

### Krok 1: Utwórz konto email w OVH

1. Zaloguj się do panelu OVH
2. Przejdź do sekcji **Web Cloud** → **E-maile**
3. Wybierz swoją domenę
4. Utwórz nowe konto email (np. `noreply@twoja-domena.pl`)

### Krok 2: Pobierz dane SMTP

Dla OVH, domyślne ustawienia SMTP to:
- **Host:** `ssl0.ovh.net`
- **Port:** `587` (STARTTLS) lub `465` (SSL)
- **Username:** `twoj-email@twoja-domena.pl`
- **Password:** hasło do konta email

### Krok 3: Ustaw zmienne środowiskowe na serwerze produkcyjnym

```bash
# Email Configuration (OVH)
MAIL_HOST=ssl0.ovh.net
MAIL_PORT=587
MAIL_USERNAME=noreply@twoja-domena.pl
MAIL_PASSWORD=twoje-haslo-ovh
MAIL_FROM_ADDRESS=noreply@twoja-domena.pl
MAIL_FROM_NAME=Twoja Firma
EMAIL_GATEWAY_TYPE=smtp
```

### Krok 4: Deploy aplikacji

Podczas wdrożenia na produkcję, upewnij się że:
1. Profil Spring Boot jest ustawiony na `prod`: `spring.profiles.active=prod`
2. Wszystkie zmienne środowiskowe są poprawnie skonfigurowane
3. Firewall pozwala na połączenia wychodzące do `ssl0.ovh.net:587`

---

## Zmiana dostawcy email

### Z Gmail na OVH

Po przejściu na produkcję, zmień zmienne środowiskowe:

```bash
# Zamiast Gmail
MAIL_USERNAME=twoj-email@gmail.com
MAIL_PASSWORD=gmail-app-password

# Użyj OVH
MAIL_HOST=ssl0.ovh.net
MAIL_PORT=587
MAIL_USERNAME=noreply@twoja-domena.pl
MAIL_PASSWORD=twoje-haslo-ovh
MAIL_FROM_ADDRESS=noreply@twoja-domena.pl
```

Aplikacja automatycznie wykryje zmiany po restarcie.

### Z SMTP na EmailLabs API

Jeśli chcesz przełączyć się na EmailLabs:

```bash
EMAIL_GATEWAY_TYPE=emaillabs
EMAILLABS_API_KEY=twoj-klucz-api
EMAILLABS_SECRET=twoj-secret
```

---

## Rozwiązywanie problemów

### Problem: "Authentication failed"

**Gmail:**
- Sprawdź czy użyłeś hasła aplikacji (nie zwykłego hasła Gmail)
- Upewnij się że weryfikacja dwuetapowa jest włączona
- Sprawdź czy hasło aplikacji jest poprawnie skopiowane (bez spacji)

**OVH:**
- Sprawdź poprawność hasła email
- Upewnij się że konto email jest aktywne w panelu OVH

### Problem: "Connection timeout"

- Sprawdź połączenie internetowe
- Sprawdź czy firewall nie blokuje portów 587 lub 465
- Dla OVH: upewnij się że używasz `ssl0.ovh.net` jako host

### Problem: Emaile nie dochodzą

1. Sprawdź logi aplikacji: `logs/marketing-automation.log`
2. Sprawdź folder SPAM w skrzynce odbiorczej
3. Dla Gmail: sprawdź https://mail.google.com/mail/u/0/#sent
4. Dla OVH: sprawdź logi SMTP w panelu OVH

### Problem: "Failed to send email"

Włącz debug logging w `application.yaml`:

```yaml
logging:
  level:
    org.springframework.mail: DEBUG
    jakarta.mail: DEBUG
```

---

## Limity wysyłki

### Gmail
- **Limit dzienny:** 500 emaili/dzień dla kont Google Workspace
- **Limit dzienny:** 100 emaili/dzień dla darmowych kont Gmail
- **Limit na odbiorcę:** 100 emaili/wiadomość

### OVH
- Limity zależą od wykupionego pakietu
- Standardowo: brak sztywnych limitów, ale zalecane max 200 emaili/godz z jednego konta
- Dla dużych wolumenów: używaj dedykowanego serwera SMTP lub usługi EmailLabs

---

## Bezpieczeństwo

⚠️ **WAŻNE:**

1. **NIE** commituj pliku `.env` do repozytorium Git
2. `.env` jest dodany do `.gitignore`
3. Używaj **haseł aplikacji** dla Gmail, nie zwykłych haseł
4. Dla produkcji: używaj menedżera sekretów (np. AWS Secrets Manager, Azure Key Vault)
5. Regularnie zmieniaj hasła email

---

## Struktura emaili transakcyjnych

Aplikacja wysyła następujące typy emaili:

### 1. Email weryfikacyjny
- **Trigger:** Rejestracja nowego użytkownika
- **Template:** `buildVerificationEmailContent()`
- **Zawiera:** 6-cyfrowy kod weryfikacyjny
- **Ważność kodu:** 24 godziny

### 2. Reset hasła
- **Trigger:** Żądanie resetu hasła
- **Template:** `buildPasswordResetEmailContent()`
- **Zawiera:** 6-cyfrowy kod resetowania
- **Ważność kodu:** 1 godzina

### 3. Email powitalny
- **Trigger:** Pomyślna weryfikacja email
- **Template:** `buildWelcomeEmailContent()`
- **Zawiera:** Powitanie i linki do zasobów

---

## Kontakt

W razie problemów z konfiguracją email, sprawdź:
- Dokumentację Spring Boot Mail: https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.email
- FAQ OVH SMTP: https://docs.ovh.com/pl/emails/
- Gmail App Passwords: https://support.google.com/accounts/answer/185833
