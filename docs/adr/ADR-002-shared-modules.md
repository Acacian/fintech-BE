# ADR-002: 공통 기능과 결제수단 도메인의 공유 모듈화

## Status
Accepted

## Context
가맹점 서비스와 사용자 서비스는 각각 JWT, 공통 예외, Redis 키 규칙, 로깅 필터를 필요로 했다.  
또한 사용자 카드, 결제수단, 읽기 전용 거래 정보는 `payment`, `appuser-manage`, `backoffice-api`가 함께 참조했다.  
서비스마다 같은 코드를 복사하면 인증 규칙과 도메인 모델이 쉽게 어긋나고, 구조적 일관성을 유지하기 어려워진다.

## Related Issues
- [Issue #2](https://github.com/Acacian/fintech-BE/issues/2)

## Decision
공통 관심사는 `common` 모듈로, 결제수단 관련 엔티티와 리포지토리는 `payment-method` 모듈로 분리한다.

- `common`: `JwtValidator`, 공통 예외 코드, `GlobalExceptionHandler`, 로깅/유틸리티
- `payment-method`: `User`, `CardInfo`, `PaymentMethod` 엔티티와 관련 리포지토리

각 서비스는 필요한 모듈만 직접 의존하고, `EntityScan` 및 `EnableJpaRepositories`로 공유 도메인을 가져온다.

## Consequences
(+) 인증과 예외 처리 규칙을 서비스 간 일관되게 유지할 수 있다.  
(+) 카드/결제수단 도메인 모델을 한 곳에서 관리할 수 있다.  
(+) 중복 코드가 줄고, 리뷰 포인트가 도메인 로직에 집중된다.  
(-) 공유 모듈의 변경이 여러 서비스의 컴파일과 테스트에 영향을 준다.  
(-) 엔티티 스캔 범위를 이해하지 못하면 의존 관계가 헷갈릴 수 있다.  
(-) 완전한 독립 배포형 MSA보다는 멀티모듈 모노레포에 가까운 결합이 남는다.
