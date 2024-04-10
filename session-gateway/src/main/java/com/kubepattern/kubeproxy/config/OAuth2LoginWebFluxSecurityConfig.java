package com.kubepattern.kubeproxy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;


import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
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

    @Value("${ide.ide-proxy-domain:kube-proxy.amdp-dev.skamdp.org}")
    private String domainUrl;

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginWebFluxSecurityConfig(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // JWK Set URI를 사용하여 NimbusReactiveJwtDecoder 인스턴스 생성
        String jwkSetUri = this.issuerUri + "/protocol/openid-connect/certs";
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }


    /*
    @Bean
    ServerSecurityContextRepository jdbcSecurityContextRepository() {
        return new JdbcSecurityContextRepository(customSecurityContextRepository, authorizedClientService);
    }

     */

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, OAuth2ClientProperties oAuth2ClientProperties) {
        HeaderWriterServerLogoutHandler handler = new HeaderWriterServerLogoutHandler(
                new ClearSiteDataServerHttpHeadersWriter(
                        ClearSiteDataServerHttpHeadersWriter.Directive.COOKIES,
                        ClearSiteDataServerHttpHeadersWriter.Directive.STORAGE,
                        ClearSiteDataServerHttpHeadersWriter.Directive.EXECUTION_CONTEXTS
                )
        );
        //KeycloakServerLogoutHandler keycloakServerLogoutHandler = new KeycloakServerLogoutHandler(handler, customSecurityContextRepository);

        http.authorizeExchange(
                exchange -> exchange.pathMatchers(
                        "/actuator/**",
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
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(authorize -> authorize.anyExchange().authenticated())
                .logout(logout -> logout
                                //.logoutHandler(keycloakServerLogoutHandler)
                                .logoutSuccessHandler((message, authentication) -> {
                                    String sessionId = message.getExchange().getRequest().getCookies().getFirst("SESSION").getValue();
                                    message.getExchange().getResponse().setStatusCode(HttpStatus.OK);
                                    String uri = this.issuerUri + "/protocol/openid-connect/logout?redirect_uri=https://" + this.domainUrl + "/list";

                                    RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
                                    successHandler.setLogoutSuccessUrl(URI.create(uri));

                                    log.debug("logoutSuccessHandler sessionId: {}", sessionId);
                                    return logoutSuccessHandler().onLogoutSuccess(message, authentication);
                                    //return handler.logout(message, authentication);
                                })
                                //.logoutSuccessHandler(logoutSuccessHandler())
                );


        // Custom Security Context 처리를 추가한다.
        //http.securityContextRepository(jdbcSecurityContextRepository());

        return http.build();
    }


    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        String uri = this.issuerUri + "/protocol/openid-connect/logout?redirect_uri=https://" + this.domainUrl + "/list";

        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create(uri));


        return successHandler;
    }

}
