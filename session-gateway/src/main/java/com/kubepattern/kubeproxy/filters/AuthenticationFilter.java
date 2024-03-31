package com.kubepattern.kubeproxy.filters;


import com.kubepattern.kubeproxy.util.ExchangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ServerSecurityContextRepository serverSecurityContextRepository;
    private final ExchangeHandler exchangeHandler;

    @Value("${kubeproxy.domain.url:kube-proxy.amdp-dev.skamdp.org}")
    private String domainUrl;


    public AuthenticationFilter(ServerSecurityContextRepository serverSecurityContextRepository,
                                ExchangeHandler exchangeHandler) {
        this.serverSecurityContextRepository = serverSecurityContextRepository;
        this.exchangeHandler = exchangeHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //log.debug("######################## AuthenticationFilter filter() host: {} path: {} uri: {}",
        //        exchange.getRequest().getHeaders().getHost(), exchange.getRequest().getPath(), exchange.getRequest().getURI());
        log.debug("###### header = {} ", exchange.getRequest().getHeaders());

        String sessionId = exchange.getRequest().getCookies().getFirst("SESSION").getValue();
        if(sessionId != null) {
            String sessionString = "SESSION=" + exchangeHandler.extractSessionId(exchange, "SESSION").get() + "; Path=/; Domain=." + this.domainUrl + "; Secure; SameSite=None";
            exchange.getResponse().getHeaders().add("Set-Cookie", sessionString);
        }
        Mono<SecurityContext> sercurityContextMono = exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof Authentication) {
                        SecurityContextHolder.getContext().setAuthentication((Authentication) principal);
                    }
                    return Mono.justOrEmpty(SecurityContextHolder.getContext());
                });


        return sercurityContextMono
                .filter(securityContext -> securityContext.getAuthentication() != null)
                .map(securityContext -> {

                    this.exchangeHandler.addTokenToResponse(exchange, securityContext)
                            .subscribe(token -> {
                                //log.debug("###### token: {}", token);
                                String cookieString = "access_token=" + token + "; Path=/; Domain=." + this.domainUrl + "; Secure; SameSite=None";
                                //String cookieString = "access_token=" + token + "; Path=/; Secure; SameSite=None";
                                //exchange.getResponse().getHeaders().setBearerAuth(token);
                                //YWYI exchange.getResponse().getHeaders().add("Set-Cookie", cookieString);

                            });
                    return (OAuth2User) securityContext.getAuthentication().getPrincipal();
                })
                .flatMap(oAuth2User -> {
                    String name = oAuth2User.getAttribute("preferred_username");
                    // String forUserName = tokenResponseUtil.extractSubdomain(exchange);
                    log.debug("######################## Authentication name: {}", name);

                    // Response에 WEBIDE_USER 쿠키를 추가 합니다.
                    String webideString = "WEBIDE_USER=" + name + "; Path=/; Domain=." + this.domainUrl + "; HttpOnly; Secure; SameSite=None";
                    exchange.getResponse().getHeaders().add("Set-Cookie", webideString);

                    log.debug("name: {}", name);
                    log.debug("###### Authentication request.headers: {}", exchange.getRequest().getHeaders());
                    log.debug("###### Authentication response.headers: {}", exchange.getResponse().getHeaders());
                    log.debug(" ");
                    String path = exchange.getRequest().getURI().getRawPath();


                    if(!name.equals("admin") && !name.equals("administrator")) {
                        String patternString = String.format("/(%s)/.*/(vscode|cli|proxy|jupyter|tensorflow)(/|$)", name);

                        Pattern pattern = Pattern.compile(patternString);
                        Matcher matcher = pattern.matcher(path);
                        if (matcher.find()) {
                            String userName = matcher.group(1);
                            log.debug("~~~~~userName: {}", userName);
                            if (!userName.equals(name)) {
                                log.debug("~~~~~~~Forbidden: {}", path);
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                        }
                    }


                    log.debug("path: {}", path);

                    return chain.filter(exchange);
                });
                //.switchIfEmpty(chain.filter(exchange));
    }


    @Override
    public int getOrder() {
        return -10000; // 필터 순서를 높게 설정하여 가장 먼저 실행되도록 합니다.
    }
}

