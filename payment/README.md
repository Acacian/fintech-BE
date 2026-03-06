# payment

`payment`는 결제 세션 생성, 승인, 취소를 담당하는 서비스입니다.

## 담당 범위

- 결제 요청 생성
- 결제 토큰 기반 상태 조회
- 카드 토큰 기반 결제 실행
- 완료된 결제 취소

## 현재 구현 범위

- 가맹점 서버가 `MERCHANT_API_KEY`로 결제 세션을 생성합니다.
- 앱 사용자가 `USER_ACCESS_TOKEN`과 등록된 `cardToken`으로 결제를 승인합니다.
- 운영 조회와 취소는 결제 완료 후 `paymentToken` 기준으로 이어집니다.
- 외부 PG의 redirect/confirm/webhook 정산 흐름을 직접 구현한 모듈은 아니며, 내부 결제 오케스트레이션에 초점을 둡니다.

## 주요 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/payments` | 결제 준비 (`MERCHANT_API_KEY`) |
| `GET` | `/api/payments/{paymentToken}` | 결제 상태 조회 |
| `PATCH` | `/api/payments` | 결제 실행 (`USER_ACCESS_TOKEN`) |
| `DELETE` | `/api/payments/{paymentToken}` | 결제 취소 (`MERCHANT_API_KEY`) |

## 의존 모듈

- `payment-method`
  - 카드, 사용자, 결제수단 관련 공유 엔티티를 참조합니다.

## 로컬 실행

### 요구사항

- Java 21
- PostgreSQL
- Redis

### 실행

```bash
./gradlew :payment:bootRun
```

### 테스트

```bash
./gradlew :payment:test
```

## 확인 경로

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- API Docs: `http://localhost:8081/v3/api-docs`

## 참고

- 이 서비스는 `payment-method` 모듈의 공유 엔티티를 사용합니다.
- 가맹점 호출은 `MERCHANT_API_KEY`, 사용자 호출은 `USER_ACCESS_TOKEN` 기준으로 검증합니다.
- 루트 README의 Quick Start와 API 요약이 이 모듈 기준으로 함께 유지됩니다.
- 현재는 `paymentToken`을 내부 결제 세션 식별자로 사용하며, 외부 승인 식별자 관리까지는 포함하지 않습니다.
