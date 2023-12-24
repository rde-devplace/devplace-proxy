package com.kubepattern.kubeproxy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Slf4j
@Configuration
public class GatewayConfigSL {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // vscode 라우팅을 위한 dynamic route 설정
                .route(r -> r.path("/{user}/vscode/**")
                        .and().method(HttpMethod.GET)
                        .or().method(HttpMethod.POST)
                        .or().method(HttpMethod.DELETE)
                        .filters(f ->
                                f.rewritePath("/{user}/vscode/(?<remaining>.*)", "/vscode/${remaining}")
                                .addRequestHeader("Host", "{user}-vscode-server-service"))
                        .uri("http://{user}-vscode-server-service:8080"))

                // SSH server 라우팅을 위한 dynamic route 설정
                .route(r -> r.path("/{user}/cli/**")
                        .and().method(HttpMethod.GET)
                        .or().method(HttpMethod.POST)
                        .or().method(HttpMethod.DELETE)
                        .filters(f ->
                                f.rewritePath("/{user}/vscode/(?<remaining>.*)", "/vscode/${remaining}")
                                        .addRequestHeader("Host", "{user}-vscode-server-service"))
                        .uri("http://{user}-vscode-server-service:8080"))

                // 사용자 port 라우팅을 위한 dynamic route 설정
                .route(r -> r.path("/{user}/proxy/{portNumber}/**")
                        .and().method(HttpMethod.GET)
                        .or().method(HttpMethod.POST)
                        .or().method(HttpMethod.DELETE)
                        .filters(f -> f.rewritePath("/{user}/proxy/{portNumber}/(?<remaining>.*)", "/proxy/${remaining}")
                                .addRequestHeader("Host", "{user}-vscode-server-service"))
                        .uri("http://{user}-vscode-server-service:{portNumber}"))

                .route("path_route", r -> r.path("/api/ideconfig/ide")
                        .and().method(HttpMethod.GET)
                        .or().method(HttpMethod.POST)
                        .or().method(HttpMethod.DELETE)
                        .uri("http://ide-operator-service:8080"))
                .build();
    }
}
