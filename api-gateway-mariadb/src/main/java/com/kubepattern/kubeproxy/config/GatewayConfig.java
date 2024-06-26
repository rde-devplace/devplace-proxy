package com.kubepattern.kubeproxy.config;

import static org.springframework.cloud.gateway.filter.factory.RewriteResponseHeaderGatewayFilterFactory.*;
import static org.springframework.cloud.gateway.support.NameUtils.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kubepattern.kubeproxy.service.ProxyRouterService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Configuration
@RefreshScope
public class GatewayConfig {

    @Autowired
    private ProxyRouterService proxyRouterService;

    @Bean
    public RouteDefinitionLocator customRouteDefinitionLocator() {
        log.info("customRouteDefinitionLocator");

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
                        if(router.isTokenRelay()) {
                            filter.addArg("tokenRelay", "true");
                        }
                        filters.add(filter);
                    }
                    // TokenRelay 필터 추가
                    /*
                    if (router.isTokenRelay()) {
                        FilterDefinition filter = new FilterDefinition();
                        filter.setName("TokenRelay");
                        filters.add(filter);
                    }
                     */

                    if(! filters.isEmpty())
                        routeDefinition.setFilters(filters);


                    return routeDefinition;
                }).collect(Collectors.toList())
        );
    }
}
