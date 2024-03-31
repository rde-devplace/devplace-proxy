package com.kubepattern.kubeproxy.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CookieFilter implements WebFilter, Ordered {
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public CookieFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.debug("######################## CookieFilter filter() host: {} path: {} uri: {}",
                    exchange.getRequest().getHeaders().getHost(), exchange.getRequest().getPath(), exchange.getRequest().getURI());
            HttpHeaders headers = exchange.getResponse().getHeaders();

            // Location 헤더 변경
            if (headers.getLocation() != null && headers.getLocation().getPath().equals("/login?error")) {
                URI newLocation = URI.create("/console/"); // 새로운 위치로 수정
                headers.setLocation(newLocation);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -9999; // 필터 순서를 높게 설정하여 가장 먼저 실행되도록 합니다.
    }
}

