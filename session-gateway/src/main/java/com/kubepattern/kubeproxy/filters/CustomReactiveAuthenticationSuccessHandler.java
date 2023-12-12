package com.kubepattern.kubeproxy.filters;

import com.google.gson.Gson;
import com.kubepattern.kubeproxy.model.SecurityContextEntity;
import com.kubepattern.kubeproxy.repo.JdbcSecurityContextRepository;

import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomReactiveAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JdbcSecurityContextRepository jdbcSecurityContextRepository;
    private final Gson gson;

    public CustomReactiveAuthenticationSuccessHandler(JdbcSecurityContextRepository jdbcSecurityContextRepository, Gson gson) {
        this.jdbcSecurityContextRepository = jdbcSecurityContextRepository;
        this.gson = gson;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();

        // 요청 및 응답 헤더 출력
        printRequestResponseHeaders(exchange);

        return Mono.empty();
    }

    private void printRequestResponseHeaders(ServerWebExchange exchange) {
        // 요청 헤더 출력
        System.out.println("Request Headers:");
        exchange.getRequest().getHeaders().forEach((name, values) ->
                System.out.println(name + ": " + String.join(", ", values)));

        // 응답 헤더 출력
        System.out.println("Response Headers:");
        exchange.getResponse().getHeaders().forEach((name, values) ->
                System.out.println(name + ": " + String.join(", ", values)));
    }

}

