package com.kubepattern.kubeproxy.config;

import com.kubepattern.kubeproxy.model.SecurityContextEntity;
import com.kubepattern.kubeproxy.repo.CustomSecurityContextRepository;
import com.kubepattern.kubeproxy.repo.JdbcSecurityContextRepository;
import com.kubepattern.kubeproxy.service.KeycloakServerLogoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;


import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.HeaderWriterServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;


import java.net.URI;


@Slf4j
@Configuration
@EnableWebFluxSecurity
public class OAuth2LoginWebFluxSecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private final CustomSecurityContextRepository customSecurityContextRepository;

    public OAuth2LoginWebFluxSecurityConfig(CustomSecurityContextRepository customSecurityContextRepository) {
        this.customSecurityContextRepository = customSecurityContextRepository;
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // JWK Set URI를 사용하여 NimbusReactiveJwtDecoder 인스턴스 생성
        String jwkSetUri = this.issuerUri + "/protocol/openid-connect/certs";
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    ServerSecurityContextRepository jdbcSecurityContextRepository() {
        return new JdbcSecurityContextRepository(customSecurityContextRepository);
    }
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, OAuth2ClientProperties oAuth2ClientProperties) {
        HeaderWriterServerLogoutHandler handler = new HeaderWriterServerLogoutHandler(
                new ClearSiteDataServerHttpHeadersWriter(
                        ClearSiteDataServerHttpHeadersWriter.Directive.CACHE
                        //ClearSiteDataServerHttpHeadersWriter.Directive.EXECUTION_CONTEXTS
                )
        );
        KeycloakServerLogoutHandler keycloakServerLogoutHandler = new KeycloakServerLogoutHandler(handler, customSecurityContextRepository);

        http.authorizeExchange(
                exchange -> exchange.pathMatchers(
                        "/actuator/**",
                        "/actuator/**",
                        "/bus/**",
                        "/list",
                        "/api/route/refresh",
                        "/listRoutes",
                        "/api/route",
                        "/api/route/**",
                        "/logout.html").permitAll()
                )
                .headers(
                        headers -> headers.frameOptions(
                                frameOptions -> frameOptions.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
                        )
                )
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(jwt-> jwt.jwtDecoder(reactiveJwtDecoder())))
                .oauth2Login(oauth2 -> oauth2
                        .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}"))
                )
                //.securityContextRepository(new WebSessionServerSecurityContextRepository())
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(authorize -> authorize.anyExchange().authenticated())
                .logout(logout -> logout
                                .logoutHandler(keycloakServerLogoutHandler)
                                .logoutSuccessHandler(logoutSuccessHandler())
                );


        // Custom Security Context 처리를 추가한다.
        http.securityContextRepository(jdbcSecurityContextRepository());
                                //.logoutSuccessHandler(handler));
        return http.build();
    }


    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        String uri = this.issuerUri + "/protocol/openid-connect/logout?redirect_uri=https://kube-proxy.amdp-dev.skamdp.org/list";

        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create(uri));


        return successHandler;
    }

}
