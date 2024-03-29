package com.kubepattern.kubeproxy.repo;

import com.google.gson.Gson;
import com.kubepattern.kubeproxy.model.OAuth2AuthenticationDetails;
import com.kubepattern.kubeproxy.model.SecurityContextEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class JdbcSecurityContextRepository implements ServerSecurityContextRepository {

    private final CustomSecurityContextRepository customSecurityContextRepository;
    private final Gson gson;

    public JdbcSecurityContextRepository(CustomSecurityContextRepository customSecurityContextRepository) {
        this.customSecurityContextRepository = customSecurityContextRepository;
        this.gson = new Gson();
    }
    @Override
    @Transactional
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        String header = exchange.getRequest().getHeaders().toString();
        log.info("JdbcSecurityContextRepository.save - header : {}", header);
        Optional<String> sessionId = JdbcSecurityContextRepository.extractSessionId(exchange, "SESSION");
        if (sessionId.isEmpty()) {
            log.info("JdbcSecurityContextRepository.save - sessionId is null");
            return Mono.empty(); // sessionId가 null일 경우, 빈 Mono 반환
        } else {
            return Mono.fromCallable(() -> {
                SecurityContextEntity entity = new SecurityContextEntity();
                entity.setSessionId(sessionId.get());
                Authentication authentication = context.getAuthentication();
                //entity.setAuthentication(gson.toJson(context.getAuthentication())); // 인증 정보를 JSON으로 직렬화하여 저장
                entity.setAuthentication(gson.toJson(authentication)); // 인증 정보를 JSON으로 직렬화하여 저장

                customSecurityContextRepository.save(entity); // 저장 작업
                return null; // Mono<Void>를 위해 null 반환
            }).then(); // Mono<Void> 반환
        }
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().toString();
        log.info("JdbcSecurityContextRepository.load -header : {}", header);
        //String sessionId = extractSessionId(exchange.getRequest().getHeaders().getFirst("Cookie"));
        Optional<String> sessionId = JdbcSecurityContextRepository.extractSessionId(exchange, "SESSION");
        if(sessionId.isEmpty()) {
            log.info("JdbcSecurityContextRepository.load - sessionId is null");
            return exchange.getPrincipal().flatMap(principal -> {
                if (principal instanceof Authentication) {
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    securityContext.setAuthentication((Authentication) principal);
                    return Mono.just(securityContext);
                }
                return Mono.empty();
            });
        } else {
            return Mono.defer(() -> {
                log.info("JdbcSecurityContextRepository.load - sessionId : {}", sessionId);
                Optional<SecurityContextEntity> optionalEntity = customSecurityContextRepository.findById(sessionId.get()); // 블로킹 호출
                if (optionalEntity.isPresent()) {
                    SecurityContextEntity entity = optionalEntity.get();
                    String authentication = entity.getAuthentication();
                    OAuth2AuthenticationDetails authDetails = gson.fromJson(authentication, OAuth2AuthenticationDetails.class);
                    // OAuth2User 객체 생성
                    OAuth2User oAuth2User = new DefaultOAuth2User(
                            authDetails.getPrincipal().getAuthorities().stream()
                                    .filter(a -> a.getAuthority() != null)
                                    .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                    .collect(Collectors.toList()),
                            authDetails.getPrincipal().getAttributes(),
                            authDetails.getPrincipal().getNameAttributeKey());

                    // OAuth2AuthenticationToken 객체 생성
                    OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(
                            oAuth2User,
                            authDetails.getAuthorities().stream()
                                    .filter(a -> a.getAuthority() != null)
                                    .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                    .collect(Collectors.toList()),
                            authDetails.getAuthorizedClientRegistrationId());

                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    securityContext.setAuthentication(authenticationToken);

                    return Mono.just(securityContext);
                } else {
                    log.info("JdbcSecurityContextRepository.load - optionalEntity is null");
                    return exchange.getPrincipal().flatMap(principal -> {
                        if (principal instanceof Authentication) {
                            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                            securityContext.setAuthentication((Authentication) principal);
                            return Mono.just(securityContext);
                        } else {
                            return Mono.empty();
                        }
                    });
                }
            });
        }
    }

/**
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.defer(() -> {
            String header = exchange.getRequest().getHeaders().toString();
            log.info("JdbcSecurityContextRepository.load -header : {}", header);
            String sessionId = extractSessionId(exchange.getRequest().getHeaders().getFirst("Cookie"));
            if(sessionId == null) {
                log.info("JdbcSecurityContextRepository.load - sessionId is null");
                return exchange.getPrincipal()
                        .flatMap(principal -> {
                            if (principal instanceof Authentication) {
                                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                                securityContext.setAuthentication((Authentication) principal);
                                return Mono.just(securityContext);
                                    }
                            return Mono.empty();
                        });
            } else {
                    log.info("JdbcSecurityContextRepository.load - sessionId : {}", sessionId);
                    Optional<SecurityContextEntity> optionalEntity =  securityContextRepository.findById(sessionId); // 블로킹 호출
                    if(optionalEntity.isPresent()) {
                        SecurityContextEntity entity = optionalEntity.get();
                        String authentication = entity.getAuthentication();
                        OAuth2AuthenticationDetails authDetails = gson.fromJson(authentication, OAuth2AuthenticationDetails.class);
                        // OAuth2User 객체 생성
                        OAuth2User oAuth2User = new DefaultOAuth2User(
                                authDetails.getPrincipal().getAuthorities().stream()
                                        .filter(a -> a.getAuthority() != null)
                                        .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                        .collect(Collectors.toList()),
                                authDetails.getPrincipal().getAttributes(),
                                authDetails.getPrincipal().getNameAttributeKey());

                        // OAuth2AuthenticationToken 객체 생성
                        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(
                                oAuth2User,
                                authDetails.getAuthorities().stream()
                                        .filter(a -> a.getAuthority() != null)
                                        .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                        .collect(Collectors.toList()),
                                authDetails.getAuthorizedClientRegistrationId());

                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                        securityContext.setAuthentication(authenticationToken);

                        return Mono.just(securityContext);
                    } else {
                        log.info("JdbcSecurityContextRepository.load - optionalEntity is null");
                        return Mono.empty();
                    }
            }
        });

    }
    **/

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
/*
    private String extractSessionId(String cookieHeader) {
        // 쿠키 헤더에서 SESSION ID를 추출하는 로직을 구현합니다.
        // 예를 들어, "SESSION=abc123; OtherCookie=value" 형태의 헤더에서 "abc123"를 추출합니다.
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("SESSION=")) {
                    return cookie.substring("SESSION=".length());
                }
            }
        }
        return null;
    }

 */

}

