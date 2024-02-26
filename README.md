# kubernetes 기반 Remote Development Environment 구성하기
## 개요
본 프로젝트는 Kubernetes 환경에서 컨테이너 기반 원격 개발 환경(Remote Development Environment)을 구성하는 프로젝트입니다. 제공 기능은 다음과 같습니다.

VSCODE Server 기반 IDE 제공
SSH Server 기반 Kubectl 지원 CLI 제공
jupyter notebook 기반 원격 개발 환경 제공 (제공 예정)
Intellij 기반 원격 개발 환경 제공 (제공 예정)


Remote Development Environment(RDE)란?
Remote Development Environment(RDE)는 개발 환경을 개발자의 로컬 PC가 아닌 EKS, AKS, GKS 등 kubernetes 환경에서 개발 환경을 제공하여 개발자가 언제 어디서나 자신의 개발환경을 쉽고 빠르게 구성하고 개발하고 테스트 할 수 있도록 합니다. 이 접근 방식은 로컬 컴퓨터가 아닌 클라우드 기반 환경에서 소프트웨어를 개발하는 방식으로 향상된 협업, 리소스 효율성 증대, 확장성 및 보안성을 제공하여 기존 방식에 비해 여러가지 장점을 제공합니다.

원격 개발은 간혹 원격 작업 공간 (workspace)와 혼동하는 경우가 있는데, 원격 작업 공간은 단순히 개발 런타임을 로컬 노트북에서 원격 인스턴스로 올기는 것을 말하는 반면, 원격 개발 환경은 Production 환경을 그대로 Kubernetes에 제공하는 것을 의미이며, 실제 애플리케이션 개발 및 운영 시 필요한 전체 개발/운영 도구 스택을 실행하여 연동 및 테스트 할수 있으며 개발자는 웹 브라우저 기반으로 이 모든 서비스를 제공 받을 수 있습니다.

상세 설명은 아래와 같다.
[Remote Development Environment](https://github.com/rde-devplace/devplace-frontend)


# devplace를위한 다양한 Gateway 지원

devplace를 위한 gateway는 다양한 형태로 제공합니다.
우선 session과 Keycloak 기반의 session-gateway를 우선 제공하고 있으며, 
향후 token 기반의 api-gateway-mariadb와 api-gateway-stateless를 제공할 예정입니다.
각 gateway의 기본적인 특성은 다음과 같습니다.

## 1. session-gateway

keycloak 기반의 ouath2 client 구조입니다.
이것의 목표는 로그인을 Gateway에서 직접 처리하고, 로그인 성공 시 Session을 유지하도록 하는 것입니다.
그러나 Session 정보와 Token 정보를 매핑하여 관리하기 위해 별도의 DB가 필요하며, 이를 위해 MariaDB를 사용합니다.
또한, Gateway가 여러 개 띄워져 있을 경우, Ingress Controller 에서 별도의 Stick Session 처리를 하도록 구성해야 합니다.
이것은 Gateway가 여러 개 띄워져 있을 경우, Session 정보가 일치하지 않는 문제를 해결하기 위함입니다.

## 2. api-gateway-mariadb

keycloak 기반의 ouath2 resource server 구조로 access token 처리합니다.
이것은 Session을 유지하지 않고, Token을 이용하여 동적인 Routing을 지원합니다.
이 구조를 적용하는 경우 별도의 Session 관리가 필요 없습니다.
그러나 동적인 Routing을 위해 별도의 DB가 계속 필요합니다. (현재는 MariaDB를 사용하고 있으나, Redis 등 다른 DB로 변경 가능합니다)

## 3. api-gateway-stateless
이 Gateway는 api-gateway-mariadb와 동일한 구조를 가지고 있으나, 동적 라우팅을 위한 DB가 필요없도록 하는 것이 목표입니다.
이것은 Kubernetes의 Pod에 적합하게 구성하기 위한 구조로 확장이 용이하고 별도의 DB 설치가 불필요합니다.
그렇게 하기 위해 RouteLocator에 코드로 직접 등록하여 관리합니다.
