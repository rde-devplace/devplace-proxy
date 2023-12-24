# 개요
본 코드는 Spring Cloud Gateway를 이용하여, Keycloak 연동을 통한 Proxy 기능을 구현한 코드이다.
라우팅 정보는 RouteLocator를 이용하여 동적으로 처리되도록 구성되어 있다. 
멀티 클러스터 환경에 배포를 고려하여 최소한의 구조로 돌아갈 수 있도록 되어 있다.
지원 기능 목록은 다음과 같다.
- Keycloak 연동
- RouteFunction을 이용한 DB 정보 기반 라우팅
- Function 기반 라우팅


