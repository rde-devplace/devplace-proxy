package com.kubepattern.kubeproxy.config;

import com.kubepattern.kubeproxy.service.ProxyRouterService;
import com.kubepattern.kubeproxy.util.ExchangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.filter.factory.RewriteResponseHeaderGatewayFilterFactory.REGEXP_KEY;
import static org.springframework.cloud.gateway.filter.factory.RewriteResponseHeaderGatewayFilterFactory.REPLACEMENT_KEY;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeFilterFactoryName;

@Slf4j
@Configuration
@RefreshScope
public class GatewayConfig {

    private final ProxyRouterService proxyRouterService;

    private final String domainUrl = "kube-proxy.amdp-dev.skamdp.org";

    public GatewayConfig(ProxyRouterService proxyRouterService, ExchangeHandler exchangeHandler) {
        this.proxyRouterService = proxyRouterService;
        //this.tokenResponseUtil = tokenResponseUtil;
    }

    @Bean
    public RouteDefinitionLocator customRouteDefinitionLocator() {

        return () -> Flux.fromIterable(proxyRouterService.findAll().stream().map(router -> {
                RouteDefinition routeDefinition = new RouteDefinition();
                routeDefinition.setId(router.getPath());
                routeDefinition.setUri(URI.create(router.getUri()));

                List<PredicateDefinition> predicates = new ArrayList<>();
                if (router.getPath() != null && !router.getPath().isEmpty()) {
                    predicates.add(new PredicateDefinition("Path=" + router.getPath()));
                }
                if (router.getMethod() != null && !router.getMethod().isEmpty()) {
                    predicates.add(new PredicateDefinition("Method=" + router.getMethod()));
                }
                if (router.getHost() != null && !router.getHost().isEmpty()) {
                    predicates.add(new PredicateDefinition("Host=" + router.getHost()));
                }
                if (router.getHeaderName() != null && !router.getHeaderName().isEmpty()
                    && router.getHeaderValue() != null && !router.getHeaderValue().isEmpty()) {
                    predicates.add(new PredicateDefinition("Header=" + router.getHeaderName() + "," + router.getHeaderValue()));
                }
                routeDefinition.setPredicates(predicates);

                List<FilterDefinition> filters = new ArrayList<>();
                if (router.getPathPattern() != null && !router.getPathPattern().isEmpty()
                    && router.getPathReplacement() != null && !router.getPathReplacement().isEmpty()) {
                    FilterDefinition filter = new FilterDefinition();
                    filter.setName(normalizeFilterFactoryName(RewritePathGatewayFilterFactory.class));
                    filter.addArg(REGEXP_KEY, router.getPathPattern());
                    filter.addArg(REPLACEMENT_KEY, router.getPathReplacement());
                    filters.add(filter);
                }
                // TokenRelay 필터 추가
                if (router.isTokenRelay()) {
                    FilterDefinition filter = new FilterDefinition();
                    filter.setName("TokenRelay");
                    filters.add(filter);
                }

                if(! filters.isEmpty())
                    routeDefinition.setFilters(filters);


            return routeDefinition;
            }).collect(Collectors.toList())
        );
    }


        /*
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        final String[] destinationUri = {"https://"};
        return builder.routes()
                .route(r -> r
                        .host("*." + domainUrl)
                        .and()
                        .path("/**")
                        .filters(f -> f.filter(new GatewayFilter() {
                            @Override
                            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                                String host = exchange.getRequest().getURI().getHost();
                                String idName = TokenResponseUtil.getFieldFromHost(host, 1);
                                String servicePrefix = TokenResponseUtil.getFieldFromHost(host, 3);
                                String portNumber = TokenResponseUtil.getFieldFromHost(host, 2);

                                destinationUri[0] = destinationUri[0] + servicePrefix + "-rde-service:" + portNumber;

                                log.debug("$$$$$$$$$$$$$$$$$$$$$$$$$$$ Request URI: {} idName={} portNumber={}", exchange.getRequest().getURI(), idName, portNumber);
                                // 동적으로 결정된 URI로 exchange의 요청 URI 변경

                                // 수정된 exchange 객체로 필터 체인 계속
                                return chain.filter(exchange);
                            }

                        }))
                        .uri(destinationUri[0]))
                .build();
    }

         */

}
