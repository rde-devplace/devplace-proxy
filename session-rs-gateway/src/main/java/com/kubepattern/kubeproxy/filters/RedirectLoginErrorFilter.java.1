package com.kubepattern.kubeproxy.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Component
public class RedirectLoginErrorFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            // HTTP 리다이렉션 상태 코드 (예: 302, 303 등) 검사
            if (response.getStatusCode() != null && response.getStatusCode().equals(HttpStatus.FOUND)) {
                // Location 헤더 값 검사 및 수정response.getHeaders().getLocation().
                String location = Objects.requireNonNull(response.getHeaders().getLocation()).toString();
                if("/login?error".equals(location)) {
                    response.getHeaders().setLocation(URI.create("/"));
                    // Authorization 헤더 클리어
                    exchange.getRequest().getHeaders().remove(HttpHeaders.AUTHORIZATION);
                }
            }
        }).then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -2;  // 필터 실행 순서 설정 (적절한 순서로 조정 가능)
    }
}
