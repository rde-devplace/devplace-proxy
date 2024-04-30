package com.kubepattern.kubeproxy.util;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class ExchangeHandler {


    public static Mono<String> addTokenToResponse(ReactiveOAuth2AuthorizedClientService authorizedClientService, SecurityContext context) {
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
                    // exchange.getResponse().getHeaders().setBearerAuth(accessTokenValue);
                    return accessTokenValue;
                });
    }

    public static Optional<String> extractSessionId(ServerWebExchange exchange, String cookieName) {
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

    public static String extractSubdomain(ServerWebExchange exchange) {
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

    public static Optional<String> extractBearerToken(ServerWebExchange exchange) {
        List<String> authorizationHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            return Optional.empty();
        }

        for (String header : authorizationHeaders) {
            if (header.toLowerCase().startsWith("bearer ")) {
                return Optional.of(header.substring(7));  // "Bearer " 다음의 문자열을 추출
            }
        }

        return Optional.empty();
    }

}


