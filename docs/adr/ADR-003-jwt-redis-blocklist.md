# ADR-003: JWT + Redis Blocklist 인증 전략

## Status
Accepted

## Context
가맹점과 앱 사용자 영역은 로그인, 로그아웃, 토큰 재발급이 모두 필요하다.  
서버 세션을 직접 들고 가면 서비스 수평 확장이 불리하고, 반대로 순수 JWT만 쓰면 로그아웃 직후 토큰을 즉시 무효화하기 어렵다.  
현재 저장소에는 `backoffice-manage`, `appuser-manage` 양쪽 모두 JWT 발급기와 Redis 기반 블록리스트 처리 코드가 이미 존재한다.

## Related Issues
- [Issue #3](https://github.com/Acacian/fintech-BE/issues/3)

## Decision
인증 전략은 서비스별 JWT 발급 + Redis Blocklist 조합으로 통일한다.

- 로그인 시 Access Token / Refresh Token 발급
- `Authorization` 헤더 검증은 JWT 필터에서 처리
- 로그아웃 시 남은 만료 시간을 TTL로 하여 Redis에 블록리스트 저장
- 재발급은 `Refresh-Token` 헤더 기반으로 처리

## Consequences
(+) 세션 저장소 없이도 인증 흐름을 단순하게 유지할 수 있다.  
(+) 로그아웃된 토큰을 즉시 무효화할 수 있다.  
(+) 사용자 서비스와 가맹점 서비스에 동일한 운영 패턴을 적용할 수 있다.  
(-) Redis 장애 시 로그아웃/블록리스트 검증에 영향이 생긴다.  
(-) JWT 발급 로직이 서비스별로 나뉘어 있어 장기적으로는 중앙 인증 서비스가 필요할 수 있다.  
(-) 토큰 정책과 Redis 키 규칙을 문서화하지 않으면 팀 간 혼선이 생길 수 있다.
