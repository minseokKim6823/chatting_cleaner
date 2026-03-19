# 안드로이드 실사용 가이드

이 폴더에는 실제 카카오톡 단체방에서 자동 경고 답장을 보내기 위한 안드로이드 브리지 스크립트가 들어 있습니다.

대상 스크립트:

- `kakao_warning_bot.js`

지원 런타임:

- 채팅 자동응답 봇
- 메신저봇 계열에서 `response(room, msg, sender, isGroupChat, replier, imageDB, packageName, isMultiChat)` 시그니처를 지원하는 환경

## 1. 서버 먼저 실행

```powershell
.\gradlew.bat bootRun
```

서버 확인:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api/bridge/health`

## 2. 휴대폰에서 접근 가능한 서버 주소 준비

휴대폰이 같은 와이파이에 붙어 있다면 PC의 로컬 IP를 사용하면 됩니다.

예시:

```text
http://192.168.0.10:8080
```

스크립트 상단의 `CONFIG.SERVER_URL`을 이 주소로 바꾸세요.

## 3. 스크립트 수정

[`kakao_warning_bot.js`](C:/Users/minseok/Desktop/my/kakaotalk_chatting_rule/bot/android/kakao_warning_bot.js) 상단에서 최소 아래 항목을 수정해야 합니다.

```javascript
SERVER_URL: "http://192.168.0.10:8080",
ALLOWED_ROOMS: ["친구들 단톡방"],
IGNORED_SENDERS: ["경고봇"]
```

설명:

- `SERVER_URL`: Spring 서버 주소
- `ALLOWED_ROOMS`: 자동 경고를 적용할 방 이름 목록. 비워두면 모든 방에 반응
- `IGNORED_SENDERS`: 봇 본인 이름. 무한 루프 방지용

## 4. 봇 앱에 스크립트 등록

1. 안드로이드 폰에 봇 구동 앱 설치
2. 새 스크립트 생성
3. `kakao_warning_bot.js` 내용을 그대로 붙여넣기
4. 알림 접근 권한 허용
5. 스크립트 활성화

## 5. 동작 확인

단톡방에서 아래 메시지를 보내면 서버가 위반으로 판단합니다.

- `010-1234-5678`
- `무료상담`
- `https://example.com`
- `시발`

정상 동작 시 봇이 단톡방에 자동으로 경고 문구를 답장합니다.

## 6. 빠른 상태 체크

단톡방에서 아래 명령을 보내면 서버 연결만 확인합니다.

```text
!경고봇상태
```

## 현재 탐지 기준

서버는 현재 아래 항목을 기준으로 자동 경고를 결정합니다.

- 개인정보 패턴
- 금칙어
- 광고성 키워드

## 주의

- 이 방식은 카카오 공식 API가 아니라 안드로이드 알림 기반 우회 방식입니다.
- 카카오톡 버전 변경 시 알림 구조가 바뀌면 봇 앱이 깨질 수 있습니다.
- 자동 경고만 현실적이고, 메시지 삭제나 강퇴 같은 공식 관리자 제어는 지원되지 않습니다.
