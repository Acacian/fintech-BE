# backoffice-manage

`backoffice-manage`는 가맹점 계정과 SDK Key를 관리하는 서비스입니다.

## 담당 범위

- 가맹점 회원가입, 로그인, 로그아웃, 토큰 재발급
- 가맹점 정보 조회 및 수정
- 비밀번호 변경과 계정 비활성화
- SDK Key 조회, 활성화, 비활성화, 검증, 재발급

## 주요 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/merchants/register` | 가맹점 회원가입 |
| `POST` | `/merchants/login` | 가맹점 로그인 |
| `POST` | `/merchants/logout` | 로그아웃 |
| `POST` | `/merchants/reissue` | 토큰 재발급 |
| `GET` | `/merchants/info` | 내 정보 조회 |
| `PUT` | `/merchants/modify` | 내 정보 수정 |
| `PUT` | `/merchants/update-password` | 비밀번호 변경 |
| `DELETE` | `/merchants/delete` | 계정 비활성화 |
| `GET` | `/sdk-key` | SDK Key 조회 |
| `POST` | `/sdk-key/deactivate` | SDK Key 비활성화 |
| `POST` | `/sdk-key/activate` | SDK Key 활성화 |
| `GET` | `/sdk-key/validate` | SDK Key 검증 |
| `POST` | `/sdk-key/regenerate` | SDK Key 재발급 |

## 의존 모듈

- `common`
  - JWT 검증, 공통 예외, 로깅 컴포넌트를 사용합니다.

## 로컬 실행

### 요구사항

- Java 21
- PostgreSQL
- Redis

### 실행

```bash
./gradlew :backoffice-manage:bootRun
```

### 테스트

```bash
./gradlew :backoffice-manage:test
```

## 확인 경로

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- API Docs: `http://localhost:8082/api-docs`

## 참고

- 가맹점 인증과 SDK Key 관리는 이 모듈이 담당합니다.
- 관리자 조회 성격의 기능은 `backoffice-api`가 별도로 담당합니다.
