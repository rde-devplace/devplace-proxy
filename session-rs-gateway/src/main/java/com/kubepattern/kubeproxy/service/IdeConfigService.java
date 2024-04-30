package com.kubepattern.kubeproxy.service;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.util.SVCPathGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class IdeConfigService {

    @Value("${ide.ide-proxy-domain:kube-proxy.amdp-dev.skamdp.org}")
    private String ideProxyDomain;

    @Value("${ide.app-domain-type:path}")
    private String appDomainType;


    public List<ProxyRouter> addRouteInit(
            String namespaceName,
            String name,
            String wsName,
            String appName) {
        List<ProxyRouter> proxyRouterList = new ArrayList<>();

        // 기본 경로와 URI 값
        List<String> paths = new ArrayList<>(Arrays.asList(
                SVCPathGenerator.generatePath(name, wsName, appName) + "/vscode/**",
                SVCPathGenerator.generatePath(name, wsName, appName) + "/cli/**",
                SVCPathGenerator.generatePath(name, wsName, appName) + "/jupyter/**"
        ));
        List<String> uris = new ArrayList<>(Arrays.asList(
                "http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":8443",
                "http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":3000",
                "http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":3333"
                //"http://" + name + "-vscode-server-service:8443",
                //"http://" + name + "-vscode-server-service:3000"
        ));

        List<String> ports = new ArrayList<>(Arrays.asList("8443", "3000", "3333"));

        List<String> patterns = new ArrayList<>(Arrays.asList(
                SVCPathGenerator.generatePath(name, wsName, appName) + "/(?<segment>.*)",
                "/(?<segment>.*)",
                "/(?<segment>.*)"
        ));
        List<String> replacements = new ArrayList<>(Arrays.asList(
                "/${segment}",
                "/${segment}",
                "/${segment}"
        ));

        // portList에 있는 각 포트에 대한 추가 경로와 URI 값을 생성
        /*
        for (String port : portList) {
            paths.add(SVCPathGenerator.generatePath(name, wsName, appName) + "/proxy/" + port + "/**");
            uris.add("http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":" + port);
            patterns.add(SVCPathGenerator.generatePath(name, wsName, appName) + "/proxy/" + port + "/(?<segment>.*)");
            replacements.add("/${segment}");
        }
         */

        for (int i = 0; i < paths.size(); i++) {
            ProxyRouter proxyRouter = new ProxyRouter();
            proxyRouter.setPath(paths.get(i));
            proxyRouter.setUri(uris.get(i));
            proxyRouter.setMethod("GET,POST,PUT,DELETE,PATCH,OPTIONS");
            proxyRouter.setPathPattern(patterns.get(i));
            proxyRouter.setPathReplacement(replacements.get(i));
            proxyRouter.setTokenRelay(true);
            proxyRouter.setUserName(name);
            proxyRouter.setSvcFullName(SVCPathGenerator.generateName(name, wsName, appName));
            proxyRouter.setPortNumber(ports.get(i));

            proxyRouterList.add(proxyRouter);
        }


        return proxyRouterList;

    }

    public List<ProxyRouter> addApplicationRoute(
            String namespaceName,
            String name,
            String wsName,
            String appName,
            List<String> portList) {

        if(appDomainType.equals("path")) {
            log.debug("addApplicationRouteForPath appDomainType: {}", appDomainType);
            return addApplicationRouteForPath(namespaceName, name, wsName, appName, portList);
        } else {
            log.debug("addApplicationRouteForSubDomain appDomainType: {}", appDomainType);
            return addApplicationRouteForSubDomain(namespaceName, name, wsName, appName, portList);
        }
    }

    private List<ProxyRouter> addApplicationRouteForPath(
            String namespaceName,
            String name,
            String wsName,
            String appName,
            List<String> portList) {
        List<ProxyRouter> proxyRouterList = new ArrayList<>();

        for (String port : portList) {

            ProxyRouter proxyRouter = new ProxyRouter();
            proxyRouter.setPath(SVCPathGenerator.generatePath(name, wsName, appName) + "/" + port + "/**");
            proxyRouter.setUri("http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":" + port);
            proxyRouter.setMethod("GET,POST,PUT,DELETE,PATCH,OPTIONS");
            //proxyRouter.setPathPattern(SVCPathGenerator.generatePath(name, wsName, appName) + "/" + port + "/(?<segment>.*)");
            proxyRouter.setPathPattern("/(?<segment>.*)");
            proxyRouter.setPathReplacement("/${segment}");
            proxyRouter.setTokenRelay(true);
            proxyRouter.setUserName(name);
            proxyRouter.setSvcFullName(SVCPathGenerator.generateName(name, wsName, appName));
            proxyRouter.setPortNumber(port);

            proxyRouterList.add(proxyRouter);
        }


        return proxyRouterList;
    }

    private List<ProxyRouter> addApplicationRouteForSubDomain(
            String namespaceName,
            String name,
            String wsName,
            String appName,
            List<String> portList) {
        List<ProxyRouter> proxyRouterList = new ArrayList<>();

        String path = "/**";
        String pattern = "/(?<segment>.*)";
        String replacement = "/${segment}";
        String method = "GET,POST,PUT,DELETE,PATCH,OPTIONS";
        String svcName = SVCPathGenerator.generateName(name, wsName, appName);
        String serviceUri = "http://" + svcName + "-rde-service." + namespaceName + ":";
        String host = svcName + "p%s." + ideProxyDomain;

        for (int i = 0; i < portList.size(); i++) {
            ProxyRouter proxyRouter = new ProxyRouter();
            proxyRouter.setPath(path);
            proxyRouter.setUri(serviceUri + portList.get(i));
            proxyRouter.setMethod(method);
            proxyRouter.setPathPattern(pattern);
            proxyRouter.setPathReplacement(replacement);
            proxyRouter.setTokenRelay(true);
            proxyRouter.setUserName(name);
            proxyRouter.setHost(String.format(host, portList.get(i)));
            proxyRouter.setPortNumber(portList.get(i));
            proxyRouter.setSvcFullName(svcName);

            proxyRouterList.add(proxyRouter);
        }

        return proxyRouterList;
    }
}
