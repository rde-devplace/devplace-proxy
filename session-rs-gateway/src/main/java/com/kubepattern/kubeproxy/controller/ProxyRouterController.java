package com.kubepattern.kubeproxy.controller;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.model.ProxyRouterId;
import com.kubepattern.kubeproxy.service.IdeConfigService;
import com.kubepattern.kubeproxy.service.ProxyRouterService;
import com.kubepattern.kubeproxy.util.SVCPathGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ProxyRouterController {

    private final ProxyRouterService proxyRouterService;

    private final RouteDefinitionLocator routeDefinitionLocator;

    private final IdeConfigService ideConfigService;

    public ProxyRouterController(   ProxyRouterService proxyRouterService,
                                    //ApplicationEventPublisher eventPublisher,
                                    RouteDefinitionLocator routeDefinitionLocator,
                                    IdeConfigService ideConfigService
    ) {
        this.proxyRouterService = proxyRouterService;
        //this.eventPublisher = eventPublisher;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.ideConfigService = ideConfigService;
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

    @PostMapping("/api/route/vscode")
    public List<ProxyRouter> addVscodeRouter(
            @RequestParam String namespace,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "")  String wsName,
            @RequestParam(required = false, defaultValue = "")  String appName,
            @RequestBody List<String> portList) {

        log.debug("addVscodeRouter namespace: {}, name: {}, wsName: {}, appName: {}, portList: {}", namespace, name, wsName, appName, portList);
        // 초기 경로 추가
        List<ProxyRouter> baseRouteList = ideConfigService.addRouteInit(namespace, name, wsName, appName);
        List<ProxyRouter> savedRouterList = new ArrayList<>();

        for (ProxyRouter proxyRouter : baseRouteList) {
            ProxyRouter savedRouter = proxyRouterService.save(proxyRouter);
            savedRouterList.add(savedRouter);
        }

        // RDE 내에서 실행되는 애플리케이션 포트 등록
        List<ProxyRouter> rdeRouteList = ideConfigService.addApplicationRoute(namespace, name, wsName, appName, portList);
        for (ProxyRouter proxyRouter : rdeRouteList) {
            ProxyRouter savedRouter = proxyRouterService.save(proxyRouter);
            savedRouterList.add(savedRouter);
        }

        getRouterList();

        return savedRouterList;
    }

    @PostMapping("/api/route/ports")
    public List<ProxyRouter> addPortRouter(
            @RequestParam String namespace,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "")  String wsName,
            @RequestParam(required = false, defaultValue = "")  String appName,
            @RequestBody List<String> portList) {

        // RDE 내에서 실행되는 애플리케이션 포트 등록
        List<ProxyRouter> proxyRouterList = ideConfigService.addApplicationRoute(namespace, name, wsName, appName, portList);
        List<ProxyRouter> savedRouterList = new ArrayList<>();
        for (ProxyRouter proxyRouter : proxyRouterList) {
            ProxyRouter savedRouter = proxyRouterService.save(proxyRouter);
            savedRouterList.add(savedRouter);
        }

        getRouterList();

        return savedRouterList;
    }

    @DeleteMapping("/api/route/ports")
    public ResponseEntity<Void> deletePortRouter(
            @RequestParam String namespace,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "")  String wsName,
            @RequestParam(required = false, defaultValue = "")  String appName,
            @RequestBody List<String> portList) {

        // RDE 내에서 실행되는 애플리케이션 포트 삭제
        for (String port : portList) {
            proxyRouterService.deleteBySvcFullNameAndPortNumber(SVCPathGenerator.generateName(name, wsName, appName), port);
        }

        getRouterList();

        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/api/route/vscode")
    public ResponseEntity<Void> deleteRouterBySvcFullName(
                                                        @RequestParam String namespace,
                                                       @RequestParam String name,
                                                       @RequestParam(required = false, defaultValue = "")  String wsName,
                                                       @RequestParam(required = false, defaultValue = "")  String appName) {
        proxyRouterService.deleteBySvcFullName(SVCPathGenerator.generateName(name, wsName, appName));
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
        if (!proxyRouterService.existsById(proxyRouter.getId())) {
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
    @DeleteMapping("/api/routes?svcFullName={svcFullName}&portNumber={portNumber}&path={path}")
    public ResponseEntity<Void> deleteRouter(@PathVariable String svcFullName, @PathVariable String portNumber, @PathVariable String path) {
        ProxyRouterId proxyRouterId = new ProxyRouterId(svcFullName, portNumber, path);
        if (!proxyRouterService.existsById(proxyRouterId)) {
            return ResponseEntity.notFound().build();
        }
        proxyRouterService.deleteById(proxyRouterId);
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

