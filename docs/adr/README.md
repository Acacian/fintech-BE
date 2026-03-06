# Architecture Decision Records

`fintech-BE` 저장소의 아키텍처 의사결정 기록입니다.  
기록 형식은 [TEMPLATE.md](./TEMPLATE.md)의 Nygard 스타일을 따릅니다.

## 상태

| ID | 제목 | 상태 | 관련 이슈 |
| --- | --- | --- | --- |
| `ADR-001` | [MSA 서비스 분리 기준](./ADR-001-msa-service-boundary.md) | Accepted | [#1](https://github.com/Acacian/fintech-BE/issues/1) |
| `ADR-002` | [공통 기능과 결제수단 도메인의 공유 모듈화](./ADR-002-shared-modules.md) | Accepted | [#2](https://github.com/Acacian/fintech-BE/issues/2) |
| `ADR-003` | [JWT + Redis Blocklist 인증 전략](./ADR-003-jwt-redis-blocklist.md) | Accepted | [#3](https://github.com/Acacian/fintech-BE/issues/3) |
| `ADR-004` | [모듈별 Docker 이미지와 Jenkins/Kubernetes 배포](./ADR-004-module-image-delivery.md) | Accepted | [#4](https://github.com/Acacian/fintech-BE/issues/4) |

## 운영 방식

1. GitHub Issue Form의 `ADR Proposal`로 논의를 시작한다.
2. 합의된 결정은 ADR 문서로 승격한다.
3. 상태가 바뀌면 해당 ADR의 `Status`를 함께 갱신한다.
4. ADR이 Accepted 또는 Rejected로 확정되면 연결된 제안 이슈를 종료한다.

## 관련 이슈

- [열린 ADR 제안 이슈 보기](https://github.com/Acacian/fintech-BE/issues?q=is%3Aissue+is%3Aopen+label%3Aadr)
- [종료된 ADR 이슈 보기](https://github.com/Acacian/fintech-BE/issues?q=is%3Aissue+is%3Aclosed+label%3Aadr)
- ADR 문서를 추가하거나 갱신할 때는 해당 문서의 `Related Issues` 섹션에 실제 이슈 번호를 함께 남긴다.
