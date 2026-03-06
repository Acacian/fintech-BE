# payment-method

`payment-method`는 결제 수단 관련 모델을 모아 둔 공유 도메인 모듈입니다.

이 모듈은 독립적으로 요청을 받는 서비스가 아니라 `appuser-manage`, `payment`,
`backoffice-api`가 함께 참조하는 엔티티와 리포지토리를 제공합니다.

## 왜 서비스가 아닌가

- 외부 API를 노출하는 컨트롤러가 없습니다.
- 포트, Swagger 경로, 별도 런타임 엔드포인트가 없습니다.
- 다른 모듈이 `implementation(project(":payment-method"))`로 직접 의존합니다.
- `PaymentMethodApplication` 클래스는 모듈 단위 부트스트랩과 테스트 컨텍스트를 위한 진입점입니다.

## 포함 구성

- 엔티티
  - `User`
  - `CardInfo`
  - `PaymentMethod`
  - `ReadOnlyPayment`
  - `ReadOnlyTransaction`
  - `CardType`, `PaymentMethodType`, `ReadOnlyPaymentStatus`, `UseYn`
- 리포지토리
  - `UserRepository`
  - `CardInfoRepository`
  - `PaymentMethodRepository`
  - `ReadOnlyTransactionRepository`

## 사용하는 모듈

- `appuser-manage`
  - 사용자, 카드, 결제수단 엔티티와 리포지토리를 사용합니다.
- `payment`
  - 결제 실행 시 카드와 결제수단 모델을 참조합니다.
- `backoffice-api`
  - 결제 이력 조회 시 공유 엔티티를 함께 스캔합니다.

## 빌드 및 테스트

```bash
./gradlew :payment-method:build
./gradlew :payment-method:test
```

## 변경 시 영향 범위

- 엔티티 필드 변경은 `appuser-manage`, `payment`, `backoffice-api`의 JPA 매핑에 영향을 줍니다.
- 리포지토리 시그니처 변경은 위 세 모듈의 컴파일과 테스트에 직접 영향을 줍니다.

## 참고

- 이 모듈은 배포 대상 서비스가 아닙니다.
- 실행 포트 대신 상위 서비스의 기능과 조회 API를 통해 사용됩니다.
