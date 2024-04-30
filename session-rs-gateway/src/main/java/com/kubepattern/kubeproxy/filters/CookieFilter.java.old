package com.kubepattern.kubeproxy.filters;

import com.kubepattern.kubeproxy.util.ExchangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
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
            HttpHeaders httpHeaders = exchange.getResponse().getHeaders();


            // Location 헤더 변경
            if (httpHeaders.getLocation() != null && httpHeaders.getLocation().getPath().equals("/login?error")) {
                log.debug("###### request path: {}  Location : {}", exchange.getRequest().getPath(), httpHeaders.getLocation());
                URI newLocation = URI.create("/"); // 새로운 위치로 수정
                httpHeaders.setLocation(newLocation);
                chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            if (request.getPath() != null && request.getPath().toString().startsWith("/login/oauth2")) {
                log.debug("###### request path: {}  Location : {}", exchange.getRequest().getPath(), httpHeaders.getLocation());


                // 변경된 쿠키 목록을 새로운 요청 객체에 설정
                ServerHttpRequest mutatedRequest = request.mutate()
                        .headers(header -> {
                            header.remove(HttpHeaders.COOKIE); // 기존 쿠키를 모두 제거
                            request.getCookies().forEach((name, cookieList) -> {
                                if (!"access_token".equals(name) && !"WEBIDE_USER".equals(name) && !"SESSION".equals(name)) { // "access_token" 제외하고 다시 추가
                                    header.addAll(name, cookieList.stream().map(HttpCookie::toString).collect(Collectors.toList()));
                                }
                                if("SESSION".equals(name)) {
                                    header.add("Cookie", "SESSION=" + "TEST");
                                }
                            });
                        })
                        .build();

                log.debug("###### mutated request Header : {}", mutatedRequest.getHeaders());
                chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
        }));
    }

    @Override
    public int getOrder() {
        return -9999; // 필터 순서를 높게 설정하여 가장 먼저 실행되도록 합니다.
    }
}

