# backoffice-api

`backoffice-api`는 관리자 조회 기능과 API Key 관리를 담당하는 서비스입니다.

## 담당 범위

- 가맹점 API Key 발급, 재발급, 조회, 비활성화
- 가맹점 결제 이력 목록 조회
- 결제 상세 조회

## 주요 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/merchants/api-keys/{merchantId}` | API Key 발급 |
| `POST` | `/merchants/api-keys/{merchantId}/reissue` | API Key 재발급 |
| `GET` | `/merchants/api-keys/{merchantId}` | API Key 조회 |
| `DELETE` | `/merchants/api-keys/{key}` | API Key 비활성화 |
| `GET` | `/merchants/payment-histories` | 결제 이력 조회 |
| `GET` | `/merchants/payment-histories/{paymentToken}` | 결제 상세 조회 |

## 의존 모듈

- `common`
  - JWT 검증과 공통 예외 처리를 사용합니다.
- `payment`
  - 결제 도메인 모델과 조회 로직을 참조합니다.
- `payment-method`
  - 결제수단 관련 공유 엔티티를 함께 스캔합니다.

## 로컬 실행

### 요구사항

- Java 21
- PostgreSQL
- Redis

### 실행

```bash
./gradlew :backoffice-api:bootRun
```

### 테스트

```bash
./gradlew :backoffice-api:test
```

## 확인 경로

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## 참고

- 이 서비스는 관리 화면용 조회와 운영 API를 제공합니다.
- 가맹점 인증과 SDK Key 발급은 `backoffice-manage`가 담당합니다.
