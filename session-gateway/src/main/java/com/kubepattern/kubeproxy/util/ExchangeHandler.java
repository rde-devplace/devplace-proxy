package com.kubepattern.kubeproxy.util;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExchangeHandler {

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private static final Pattern HOST_PATTERN =
            Pattern.compile("^(.+?)z([0-9]+)\\.kube-proxy\\.amdp-dev\\.skamdp\\.org$");
    private static final Pattern SUBDOMAIN_PATTERN =
            Pattern.compile("^([^z]+)");

    public static final int EXTRACT_USER_NAME = 1;
    public static final int EXTRACT_PORT_NUMBER = 2;
    public static final int EXTRACT_SERVICE_NAME = 3;
    public static final String NONE = "none";

    public ExchangeHandler(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public Mono<String> addTokenToResponse(ServerWebExchange exchange, SecurityContext context) {
        return Mono.justOrEmpty(context.getAuthentication())
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(authenticationToken -> authorizedClientService.loadAuthorizedClient(
                        authenticationToken.getAuthorizedClientRegistrationId(),
                        authenticationToken.getName())
                )
                .map(authorizedClient -> {
                    OAuth2AuthorizedClient client = (OAuth2AuthorizedClient) authorizedClient;
                    String accessTokenValue = client.getAccessToken().getTokenValue();
                    return accessTokenValue;
                    // exchange.getResponse().getHeaders().setBearerAuth(accessTokenValue);
                });
    }

    public Optional<String> extractSessionId(ServerWebExchange exchange, String cookieName) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        List<String> cookies = headers.get(HttpHeaders.COOKIE);

        if (cookies != null) {
            for (String cookie : cookies) {
                String[] cookiePairs = cookie.split("; ");
                for (String cookiePair : cookiePairs) {
                    String[] keyValue = cookiePair.split("=");
                    if (keyValue.length == 2 && cookieName.equals(keyValue[0])) {
                        return Optional.of(keyValue[1]);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public String extractSubdomain(ServerWebExchange exchange) {
        String host = exchange.getRequest().getURI().getHost();
        String[] parts = host.split("\\.");

        // 'parts' 배열의 첫 번째 요소는 서브도메인입니다 (예: 'himang10' from 'himang10.skadmp.org')
        // 도메인이 'www.example.com' 또는 'example.com' 형태일 수 있으므로, 길이 체크가 필요합니다.
        if (parts.length > 2) {
            return parts[0]; // 서브도메인 반환
        } else {
            return ""; // 서브도메인이 없는 경우, 빈 문자열 반환
        }
    }


    public static String getFieldFromHost(String host, Integer type) {
        Matcher matcher = HOST_PATTERN.matcher(host);

        switch(type) {
            case EXTRACT_USER_NAME:  // host에 포함된 계정 이름 추출
                if (matcher.matches()) {
                    // himang10-8080.kube-proxy.amdp-dev.skamdp.org 형태의 host에서 himanag10을 추출
                    String firstName = matcher.group(1);
                    Matcher subDomainMatcher = SUBDOMAIN_PATTERN.matcher(firstName);
                    String sdUser = subDomainMatcher.find()? subDomainMatcher.group(1): "none";
                    return sdUser;
                }
                break;
            case EXTRACT_PORT_NUMBER: // host에 포함된 포트 번호 추출
                if (matcher.matches()) {
                    // himang10-8080.kube-proxy.amdp-dev.skamdp.org 형태의 host에서 8080을 추출
                    String portNumber = matcher.group(2);
                    return portNumber;
                }
                break;
            case EXTRACT_SERVICE_NAME: // host에 포함된 서비스 이름 추출
                if (matcher.matches()) {
                    String serviceNamePrefix = matcher.group(1);
                    return serviceNamePrefix;
                }
                break;
        }
        return NONE;
    }

}


