# kakaotalk_chatting_rule

Spring Boot API server for:

- receiving Kakao chatbot webhook requests
- judging chat-policy violations
- storing violation and restriction history
- managing manual admin restrictions

## Requirements

- Java 21
- PowerShell or another terminal

## Run Locally

1. Check `.env`.

```powershell
Get-Content .env
```

Example:

```properties
KAKAO_ADMIN_KEY=your-kakao-admin-key
```

2. Start the app.

```powershell
.\gradlew.bat bootRun
```

3. Open the app on `http://localhost:8080`.

If port `8080` is already in use, change `server.port` in `src/main/resources/application.yml`.

## Configuration

- Port: `8080`
- Database: H2 in-memory
- H2 console: `http://localhost:8080/h2-console`
- Main config: `src/main/resources/application.yml`

Because the database is in-memory, all members, violations, and restrictions are reset when the app restarts.

## Main APIs

### 1) Kakao webhook test

This simulates a Kakao chatbot callback.

```powershell
$body = @{
  userRequest = @{
    timezone = "Asia/Seoul"
    utterance = "010-1234-5678"
    lang = "ko"
    user = @{
      id = "user-123"
      type = "accountId"
    }
  }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/kakao/webhook" `
  -ContentType "application/json" `
  -Body $body
```

Current judgment logic checks:

- personal info pattern
- forbidden words
- advertisement keywords

If a violation is detected, the app stores a violation record and creates a restriction result such as `WARNING`, `TEMPORARY_BAN`, or `PERMANENT_BAN`.

### 2) Create admin restriction

`/api/admin/**` requires authentication.

Right now the project uses Spring Security's default generated password. When the app starts, the log prints a line like this:

```text
Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Username: `user`

```powershell
$cred = Get-Credential

$body = @{
  kakaoUserId = "user-123"
  level = "WARNING"
  reason = "manual restriction test"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/admin/restrictions" `
  -Authentication Basic `
  -Credential $cred `
  -ContentType "application/json" `
  -Body $body
```

Allowed `level` values:

- `WARNING`
- `TEMPORARY_BAN`
- `PERMANENT_BAN`

### 3) List restrictions for a user

```powershell
$cred = Get-Credential

Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/admin/restrictions?kakaoUserId=user-123" `
  -Authentication Basic `
  -Credential $cred
```

### 4) Get one restriction by id

```powershell
$cred = Get-Credential

Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/admin/restrictions/1" `
  -Authentication Basic `
  -Credential $cred
```

## Seed Data

On startup, the app inserts default violation policies automatically.

## Development Commands

Run tests:

```powershell
.\gradlew.bat test
```

## Known Gaps

- Tests currently fail because the test code expects Swagger-related setup that is not fully matched by the current dependency/configuration set.
- Some existing source strings in the project appear to have encoding issues.
- `SPAM` exists in seeded policy data, but the current judgment flow does not actively detect spam yet.
- `KakaoApiClient` is still a stub and does not send real external Kakao API requests yet.
