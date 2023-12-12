# 개요
본 코드는 Spring Cloud Gateway를 이용하여, Keycloak 연동을 통한 Proxy 기능을 구현한 코드이다.
라우팅은 DB에 저장된 정보를 기반으로 수행된다. 
지원 기능 목록은 다음과 같다.
- Keycloak 연동
- RouteFunction을 이용한 DB 정보 기반 라우팅
- Function 기반 라우팅


# ProxyRouter 등록 예시
Proxy Router field 는 다음과 같다.
```java
@Entity
@Data
public class ProxyRouter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 필수
    private String path;  // 필수
    private String uri;   // 필수
    private String method;
    private String host;
    private String headerName;
    private String headerValue;
    private String pathPattern; // New field for RewritePath filter's pattern (e.g. "/api/v1/users/(?<segment>.*)")
    private String pathReplacement; // New field for RewritePath filter's replacement value (e.g. "/todo/${segment}")

    // getters, setters, and other necessary methods...
}
```
id, path, uri는 필수적으로 등록되어야 한다.

## 기본 적인 라우팅
path: /todo/**
uri: http://localhost:8080/todo/**

## Path 를 다르게 Routing 

path: /test/**
uri: http://localhost:8080
pathPattern: /test/(?<segment>.*)
patnPReplacement: /mytodo/${segment}

## method
모든 Method를 지원하고자 할 경우에는 blank
특정 Method만 지원하고자 할 경우에는 GET, POST, PUT, DELETE 등을 콤마(,)로 구분하여 입력


## Optional
host field는 Request Haeder 의 Host 값이 일치하는 요청만 처리하도록 한다. 
예를들어, curl kube-pattern.amdp-dev.skadmp.org/k9/wetty 명령을 실행하면, Host 헤더는 kube-pattern.amdp-dev.skadmp.org로 설정됩니다.


