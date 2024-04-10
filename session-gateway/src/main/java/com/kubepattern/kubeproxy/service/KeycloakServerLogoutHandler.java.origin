package com.kubepattern.kubeproxy.service;

import java.util.Optional;

import com.kubepattern.kubeproxy.repo.JdbcSecurityContextRepository;
import com.kubepattern.kubeproxy.repo.CustomSecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.HeaderWriterServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class KeycloakServerLogoutHandler implements ServerLogoutHandler {

    private final HeaderWriterServerLogoutHandler headerWriterLogoutHandler;

    final
    CustomSecurityContextRepository customSecurityContextRepository;

    public KeycloakServerLogoutHandler(HeaderWriterServerLogoutHandler headerWriterLogoutHandler,
                                       CustomSecurityContextRepository customSecurityContextRepository) {
        this.headerWriterLogoutHandler = headerWriterLogoutHandler;
        this.customSecurityContextRepository = customSecurityContextRepository;
    }

    private static Optional<String> extractSessionValue(ServerWebExchange exchange) {
        return JdbcSecurityContextRepository.extractSessionId(exchange, "SESSION");
    }

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        Optional<String> sessionValue = extractSessionValue(exchange.getExchange());

        // 세션 값 처리
        return headerWriterLogoutHandler.logout(exchange, authentication)
                .then(Mono.fromRunnable(() -> performPostLogoutActions(sessionValue)));
        // 여기에 세션 ID를 사용하는 로직 추가
    }

    private void performPostLogoutActions(Optional<String> sessionValue) {
        // 여기에 세션 ID를 사용하는 로직 추가
        sessionValue.ifPresent(customSecurityContextRepository::deleteById);

    }


}

