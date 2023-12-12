package com.kubepattern.kubeproxy.controller;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.service.ProxyRouterService;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

@RestController
public class ProxyRouterController {

    private final ProxyRouterService proxyRouterService;

    //private final ApplicationEventPublisher eventPublisher;

    private final RouteDefinitionLocator routeDefinitionLocator;


    public ProxyRouterController(   ProxyRouterService proxyRouterService,
                                    //ApplicationEventPublisher eventPublisher,
                                    RouteDefinitionLocator routeDefinitionLocator
    ) {
        this.proxyRouterService = proxyRouterService;
        //this.eventPublisher = eventPublisher;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    /**
     * ProxyRouter 엔터티를 추가한다.
     */
    @PostMapping("/api/route")
    public ProxyRouter addRouter(@RequestBody ProxyRouter proxyRouter) {
      ProxyRouter savedRouter = proxyRouterService.save(proxyRouter);
        getRouterList();
        return savedRouter;
    }



    @DeleteMapping("/api/route/user/{userName}")
    public ResponseEntity<Void> deleteRouterByUserName(@PathVariable String userName) {
        proxyRouterService.deleteByUserName(userName);
        getRouterList();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/id")
    public String getUserId(Principal principal) {

        return principal.getName();
    }

    @GetMapping("/user/name")
    public String getUser(@AuthenticationPrincipal OAuth2User user) {
        String name = user.getAttribute("preferred_username");
        return name;
    }


    /**
     * ProxyRouter 엔터티를 수정한다.
     */
    @PutMapping("/api/route/{path}")
    public ResponseEntity<ProxyRouter> modifyRouter(@PathVariable String path, @RequestBody ProxyRouter proxyRouter) {
        if (!proxyRouterService.existsById(path)) {
            return ResponseEntity.notFound().build();
        }
        proxyRouter.setPath(path);
        ProxyRouter updatedRouter = proxyRouterService.save(proxyRouter);
        proxyRouterService.refreshRoutes();
        //eventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return ResponseEntity.ok(updatedRouter);
    }



    /**
     * ProxyRouter 엔터티를 삭제한다.
     */
    @DeleteMapping("/api/route/{path}")
    public ResponseEntity<Void> deleteRouter(@PathVariable String path) {
        if (!proxyRouterService.existsById(path)) {
            return ResponseEntity.notFound().build();
        }
        proxyRouterService.deleteById(path);
        proxyRouterService.refreshRoutes();
        //eventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 ProxyRouter 엔터티 목록을 반환한다.
     */
    @GetMapping("/api/route/refresh")
    public void getRouterList() {
        proxyRouterService.refreshRoutes();
        //eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @GetMapping("/list")
    public Flux<RouteDefinition> listRoutes() {
        //proxyRouterService.refreshRoutes();
        //eventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return routeDefinitionLocator.getRouteDefinitions();
    }
}

