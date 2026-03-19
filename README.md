# kakaotalk_chatting_rule

카카오톡 챗봇 웹훅 요청을 받아 채팅 위반 여부를 판별하고, 위반 이력과 제재 이력을 관리하는 Spring Boot API 서버입니다.

## 프로젝트 개요

이 프로젝트는 아래 흐름을 기준으로 동작합니다.

1. 카카오 챗봇 웹훅 요청을 수신합니다.
2. 메시지에서 개인정보, 금칙어, 광고성 문구를 검사합니다.
3. 위반이 감지되면 위반 기록을 저장합니다.
4. 정책 기준에 따라 `WARNING`, `TEMPORARY_BAN`, `PERMANENT_BAN` 제재를 적용합니다.
5. 관리자는 별도의 관리자 API로 수동 제재를 등록하거나 조회할 수 있습니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.3
- Spring Web
- Spring Data JPA
- Spring Security
- H2 Database
- springdoc-openapi (Swagger UI)

## 실행 환경

- Java 21
- PowerShell 또는 다른 터미널

## 설정 파일

메인 설정 파일은 `src/main/resources/application.yml` 입니다.

민감 정보는 `.env` 파일로 분리해서 사용합니다.

예시:

```env
KAKAO_ADMIN_KEY=your_kakao_admin_key_here
```

현재 애플리케이션은 `.env` 파일의 `KAKAO_ADMIN_KEY` 값을 읽어서 `kakao.api.admin-key` 설정에 연결합니다.

## 로컬 실행

1. `.env` 파일에 카카오 어드민 키를 입력합니다.
2. 애플리케이션을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

기본 포트는 `8080`입니다.

## 확인 가능한 주소

- 애플리케이션: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`
- 브리지 상태 확인: `http://localhost:8080/api/bridge/health`

## 주요 기능

### 실제 단톡방 자동 경고 답장

실제 카카오톡 단체방에 자동 경고 답장을 붙이려면 서버만으로는 안 되고, 안드로이드 알림 기반 봇 스크립트가 추가로 필요합니다.

이 프로젝트에는 바로 붙여 넣어 쓸 수 있는 안드로이드 브리지 스크립트가 포함되어 있습니다.

- 스크립트: `bot/android/kakao_warning_bot.js`
- 가이드: `bot/android/README.md`

### 1. 카카오 웹훅 수신

- 경로: `POST /api/kakao/webhook`
- 인증: 필요 없음

요청 예시:

```json
{
  "userRequest": {
    "timezone": "Asia/Seoul",
    "utterance": "010-1234-5678",
    "lang": "ko",
    "user": {
      "id": "user-123",
      "type": "accountId"
    }
  }
}
```

PowerShell 예시:

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

현재 위반 탐지 기준:

- 개인정보 패턴
- 금칙어 포함 여부
- 광고성 키워드 포함 여부

## 관리자 API

관리자 API 경로는 `/api/admin/**` 이며 인증이 필요합니다.

현재 프로젝트는 Spring Security 기본 설정 상태라서, 애플리케이션 실행 시 로그에 출력되는 기본 계정을 사용해야 합니다.

- username: `user`
- password: 애플리케이션 시작 로그에 출력되는 generated password

### 1. 수동 제재 등록

- 경로: `POST /api/admin/restrictions`

요청 예시:

```json
{
  "kakaoUserId": "user-123",
  "level": "WARNING",
  "reason": "manual restriction test"
}
```

허용되는 `level` 값:

- `WARNING`
- `TEMPORARY_BAN`
- `PERMANENT_BAN`

PowerShell 예시:

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

### 2. 사용자 제재 목록 조회

- 경로: `GET /api/admin/restrictions?kakaoUserId=user-123`

```powershell
$cred = Get-Credential

Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/admin/restrictions?kakaoUserId=user-123" `
  -Authentication Basic `
  -Credential $cred
```

### 3. 단건 제재 조회

- 경로: `GET /api/admin/restrictions/{restrictionId}`

```powershell
$cred = Get-Credential

Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/admin/restrictions/1" `
  -Authentication Basic `
  -Credential $cred
```

## 초기 데이터

애플리케이션 시작 시 기본 위반 정책이 자동으로 저장됩니다.

현재 초기화되는 정책 유형:

- `FORBIDDEN_WORD`
- `ADVERTISEMENT`
- `SPAM`
- `PERSONAL_INFO`

## 데이터 저장 방식

현재 데이터베이스는 H2 In-Memory를 사용합니다.

따라서 애플리케이션을 재시작하면 아래 데이터는 모두 초기화됩니다.

- 회원 정보
- 위반 기록
- 제재 기록
- 정책 데이터

## 개발 명령어

애플리케이션 실행:

```powershell
.\gradlew.bat bootRun
```

테스트 실행:

```powershell
.\gradlew.bat test
```

## 현재 제약 사항

- `KakaoApiClient`는 아직 실제 카카오 외부 API 호출 로직이 완성된 상태는 아닙니다.
- 관리자 인증은 임시로 Spring Security 기본 인증을 사용합니다.
- 일부 소스 문자열에 인코딩 문제가 남아 있어 로그나 메시지가 깨져 보일 수 있습니다.
- 정책 데이터에 `SPAM`은 존재하지만 현재 판단 로직은 개인정보, 금칙어, 광고 위주로 동작합니다.
