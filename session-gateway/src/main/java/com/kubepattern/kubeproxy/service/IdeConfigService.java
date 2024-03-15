package com.kubepattern.kubeproxy.service;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.util.SVCPathGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class IdeConfigService {

    public List<ProxyRouter> addIdeRoute(
            String namespaceName,
            String name,
            String wsName,
            String appName,
            List<String> portList) {
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
        for (String port : portList) {
            paths.add(SVCPathGenerator.generatePath(name, wsName, appName) + "/proxy/" + port + "/**");
            uris.add("http://" + SVCPathGenerator.generateName(name, wsName, appName) + "-rde-service." + namespaceName + ":" + port);
            patterns.add(SVCPathGenerator.generatePath(name, wsName, appName) + "/proxy/" + port + "/(?<segment>.*)");
            replacements.add("/${segment}");
        }

        for (int i = 0; i < paths.size(); i++) {
            ProxyRouter proxyRouter = new ProxyRouter();
            proxyRouter.setPath(paths.get(i));
            proxyRouter.setUri(uris.get(i));
            proxyRouter.setMethod("GET,POST,PUT,DELETE,PATCH,OPTIONS");
            proxyRouter.setPathPattern(patterns.get(i));
            proxyRouter.setPathReplacement(replacements.get(i));
            proxyRouter.setTokenRelay(true);
            proxyRouter.setUserName(name);

            proxyRouterList.add(proxyRouter);
        }

        return proxyRouterList;

    }
}
