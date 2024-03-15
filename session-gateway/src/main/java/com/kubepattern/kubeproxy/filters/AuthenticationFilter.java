package com.kubepattern.kubeproxy.filters;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ServerSecurityContextRepository serverSecurityContextRepository;

    public AuthenticationFilter(ServerSecurityContextRepository serverSecurityContextRepository) {
        this.serverSecurityContextRepository = serverSecurityContextRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        // headers.remove("P3P");

        Mono<SecurityContext> sercurityContextMono = exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof Authentication) {
                        SecurityContextHolder.getContext().setAuthentication((Authentication) principal);
                    }
                    return Mono.justOrEmpty(SecurityContextHolder.getContext());
                });


        return sercurityContextMono
                //.filter(securityContext -> securityContext.getAuthentication() != null && securityContext.getAuthentication().getPrincipal() instanceof OAuth2User)
                //.map(securityContext -> (OAuth2User) securityContext.getAuthentication().getPrincipal())
                .filter(securityContext -> securityContext.getAuthentication() != null)
                .map(securityContext -> {
                    // header에 path /login/oauth2/code/kube-proxy-renew? 가 있는지를 확인해서 있으면
                    // securityContextRepository.save(exchange, securityContext)를 호출한다.
                    // 성공적으로 로그인 했을 때 session 정볼르 등록
                    /*
                    Optional<String> path = Optional.ofNullable(exchange.getRequest().getURI().getPath());
                    //Optional<String> location = Optional.ofNullable(exchange.getResponse().getHeaders().getFirst("Location"));
                    log.info("--------------- filter - location: {}", path);
                    if(path.isPresent()) {
                        if (path.get().contains("/oauth2/authorization/")) {
                            log.info("--------------- include serverSecurityContextRepository.save - location: {}", path);
                            serverSecurityContextRepository.save(exchange, securityContext);
                        }
                    }
                     */
                    return (OAuth2User) securityContext.getAuthentication().getPrincipal();
                })
                .flatMap(oAuth2User -> {
                    String name = oAuth2User.getAttribute("preferred_username");

                    log.info("name: {}", name);
                    String path = exchange.getRequest().getURI().getRawPath();

                    if(!name.equals("admin") && !name.equals("administrator")) {
                        String patternString = String.format("/(%s)/.*/(vscode|cli|proxy|jupyter|tensorflow)(/|$)", name);

                        Pattern pattern = Pattern.compile(patternString);
                        // Pattern pattern = Pattern.compile("/([^/]+)/(vscode|cli|proxy|jupyter)(/|$)");
                        Matcher matcher = pattern.matcher(path);
                        if (matcher.find()) {
                            String userName = matcher.group(1);
                            log.info("userName: {}", userName);
                            if (!userName.equals(name)) {
                                log.info("Forbidden: {}", path);
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                        }
                    }

                    //log.info("authorization=" + exchange.getRequest().getHeaders().getFirst("Authorization"));
                    log.info("cookie=" + exchange.getRequest().getHeaders().getFirst("Cookie"));

                    log.info("path: {}", path);

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }


    @Override
    public int getOrder() {
        return -10000; // 필터 순서를 높게 설정하여 가장 먼저 실행되도록 합니다.
    }
}

