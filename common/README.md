# common

`common`은 서비스 전반에서 공통으로 사용하는 지원 모듈입니다.

JWT 검증, 예외 코드와 응답 포맷, 요청 로깅, 범용 유틸리티처럼 도메인과
무관하게 반복되는 기능을 한 곳에서 관리합니다.

## 왜 서비스가 아닌가

- 외부 API를 노출하는 컨트롤러가 없습니다.
- 포트, Swagger 경로, 별도 비즈니스 엔드포인트가 없습니다.
- 다른 모듈이 `implementation(project(":common"))`로 직접 의존합니다.
- `CommonApplication` 클래스는 모듈 단위 부트스트랩과 테스트 컨텍스트를 위한 진입점입니다.

## 제공 기능

- 인증
  - `JwtValidator`
- 예외 처리
  - `GlobalExceptionHandler`
  - `AuthErrorCode`, `MerchantErrorCode`, `PaymentErrorCode`, `CardErrorCode`, `SdkErrorCode`, `CommonErrorCode`
  - `ErrorResponse`
- 로깅
  - `RequestLoggingFilter`
  - `LogExecutionTime`
  - `LogExecutionTimeAspect`
- 상수 및 유틸리티
  - `RedisKeys`
  - `JsonUtils`
  - `UUIDGenerator`

## 사용하는 모듈

- `backoffice-manage`
- `backoffice-api`
- `appuser-manage`

## 빌드 및 테스트

```bash
./gradlew :common:build
./gradlew :common:test
```

## 변경 시 영향 범위

- 예외 코드와 응답 포맷 변경은 API 응답 계약에 영향을 줄 수 있습니다.
- JWT 검증 로직 변경은 인증이 필요한 모든 서비스에 영향을 줍니다.
- 로깅 필터와 AOP 변경은 공통 운영 로그 형식에 영향을 줍니다.

## 참고

- `common`은 독립 배포 대상이 아니라 서비스들이 함께 사용하는 공통 지원 모듈입니다.
- `GlobalExceptionHandler`는 공통 컴포넌트이지만 외부 비즈니스 API를 제공하는 것은 아닙니다.
