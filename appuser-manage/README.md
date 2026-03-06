# appuser-manage

`appuser-manage`는 앱 사용자 계정, 카드, 개인 거래 조회를 담당하는 서비스입니다.

## 담당 범위

- 사용자 회원가입, 로그인, 로그아웃, 토큰 재발급
- 내 정보 조회 및 수정
- 카드 등록, 조회, 삭제, 결제 비밀번호 변경
- 사용자 기준 거래 내역 조회

## 주요 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/app-users/register` | 사용자 회원가입 |
| `POST` | `/app-users/login` | 사용자 로그인 |
| `POST` | `/app-users/logout` | 로그아웃 |
| `POST` | `/app-users/reissue` | 토큰 재발급 |
| `GET` | `/app-users/info` | 내 정보 조회 |
| `PUT` | `/app-users/modify` | 내 정보 수정 |
| `PUT` | `/app-users/update-password` | 비밀번호 변경 |
| `DELETE` | `/app-users/delete` | 계정 비활성화 |
| `POST` | `/app-users/cards/register` | 카드 등록 |
| `GET` | `/app-users/cards` | 카드 목록 조회 |
| `GET` | `/app-users/cards/{cardToken}` | 카드 상세 조회 |
| `PUT` | `/app-users/cards/{cardToken}/payment-password` | 결제 비밀번호 변경 |
| `GET` | `/app-users/cards/{cardToken}/valid` | 카드 유효성 확인 |
| `DELETE` | `/app-users/cards/{cardToken}` | 카드 삭제 |
| `GET` | `/api/info/transactions` | 전체 거래 내역 조회 |
| `GET` | `/api/info/transactions/by-card/{cardToken}` | 카드별 거래 내역 조회 |

## 의존 모듈

- `common`
  - JWT 검증, 공통 예외, 로깅 컴포넌트를 사용합니다.
- `payment-method`
  - 사용자, 카드, 결제수단 관련 공유 엔티티와 리포지토리를 사용합니다.

## 로컬 실행

### 요구사항

- Java 21
- PostgreSQL
- Redis

### 실행

```bash
./gradlew :appuser-manage:bootRun
```

### 테스트

```bash
./gradlew :appuser-manage:test
```

## 확인 경로

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- API Docs: `http://localhost:8083/api-docs`

## 참고

- 카드와 결제수단 모델은 `payment-method` 모듈에서 공유합니다.
- 거래 조회 데이터는 다른 서비스가 적재한 결제 및 거래 모델을 함께 참조합니다.
