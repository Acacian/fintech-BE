# payment

`payment`는 결제 준비, 조회, 실행, 취소를 담당하는 서비스입니다.

## 담당 범위

- 결제 요청 생성
- 결제 토큰 기반 상태 조회
- 카드 토큰 기반 결제 실행
- 완료된 결제 취소

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
