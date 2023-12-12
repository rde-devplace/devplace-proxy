# devplace를위한 다양한 Gateway 지

devplace 구성을 위한 다양한 gateay를 구성
## 1. session-gateway

keycloak 기반의 ouath2 client 구조
별도의 Session 관리를 수행해야 함. (그리고 초기 로그인 시 Session을 유지하기 위해 Ingress에서 Stick Session을 처리하도록 구성 필요.
이렇게 하지 않으면 로그인 프로세스에서 다른 Proxy로 넘어가게 되며 이로 인해 Session 불일치 문제가 발생.

## 2. api-gateway-mariadb

keycloak 기반의 ouath2 resource server 구조로 access token 처리
동적인 Routing을 위한 MariaDB를 사용
이 구조는 Replication 이 여러개가 되는 경우
이 구조는 Token을 이용하기 때문에 별도의 Session 관리가 필요 없음

## 3. api-gateway-stateless

keycloak 기반의 ouath 2 resrouce Server 구조로 devplace-api-gateway-mariadb와 동일.
단, 고정된 Routing을 지원하기 위해 RouteLocator에 코드로 직접 등록하여 관리
이것은 Kubernetes의 Pod에 적합하게 구성하기 위한 구조로 확장이 용이하고 별도의 DB 설치가 불필요.
