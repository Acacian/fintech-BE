# ADR-004: 모듈별 Docker 이미지와 Jenkins/Kubernetes 배포

## Status
Accepted

## Context
도메인을 나눴다면 배포 역시 모듈 단위로 다룰 수 있어야 한다.  
현재 저장소는 루트 `Dockerfile`에서 `ARG MODULE`을 받아 개별 모듈 JAR을 빌드할 수 있고, `Jenkinsfile`과 `k8s/` 디렉터리도 함께 유지하고 있다.  
코드와 함께 빌드, 이미지 생성, 배포 경로까지 일관되게 관리할 필요가 있다.

## Related Issues
- [Issue #4](https://github.com/Acacian/fintech-BE/issues/4)

## Decision
배포 단위는 모듈별 Docker 이미지로 고정하고, Jenkins 파이프라인에서 빌드와 이미지 푸시를 수행한 뒤 Kubernetes에 배포한다.

- 루트 `Dockerfile`은 `--build-arg MODULE=...` 방식으로 재사용한다.
- `Jenkinsfile`은 빌드, 테스트, Docker push, Kubernetes rollout 단계를 가진다.
- 배포 매니페스트는 저장소의 `k8s/` 하위에 함께 버전 관리한다.

## Consequences
(+) 서비스별로 이미지 버전과 배포 이력을 분리해서 관리할 수 있다.  
(+) 저장소만 보더라도 개발부터 배포까지의 흐름을 설명할 수 있다.  
(+) 기능 수정 시 필요한 서비스만 선택적으로 재배포하는 구조를 만들 수 있다.  
(-) Jenkinsfile과 Kubernetes 매니페스트를 계속 함께 유지보수해야 한다.  
(-) 로컬 실행 설정과 배포 설정이 어긋나면 문서 신뢰도가 떨어질 수 있다.  
(-) 서비스 수가 늘수록 파이프라인과 인프라 운영 비용도 같이 증가한다.
