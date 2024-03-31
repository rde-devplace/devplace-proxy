package com.kubepattern.kubeproxy.service;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.model.ProxyRouterId;
import com.kubepattern.kubeproxy.repo.ProxyRouterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ProxyRouterService {

    private final ApplicationEventPublisher eventPublisher;
    private final ProxyRouterRepository proxyRouterRepository;

    @Autowired
    public ProxyRouterService(ApplicationEventPublisher eventPublisher, ProxyRouterRepository proxyRouterRepository) {
        this.eventPublisher = eventPublisher;
        this.proxyRouterRepository = proxyRouterRepository;
    }


    public void refreshRoutes() {
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public List<ProxyRouter> findAll() {
        return proxyRouterRepository.findAll();
    }

    @Transactional
    public ProxyRouter save(ProxyRouter proxyRouter) {
        return proxyRouterRepository.save(proxyRouter);
    }

    public void deleteById(ProxyRouterId routerId) {
        proxyRouterRepository.deleteById(routerId);
    }

    public void delete(ProxyRouter router) {
        proxyRouterRepository.delete(router);
    }

    public ProxyRouter update(ProxyRouter router) {
        ProxyRouterId routerId = new ProxyRouterId(router.getUserName(), router.getSvcFullName(), router.getPortNumber());
        ProxyRouter existingRouter = proxyRouterRepository.findById(routerId).orElse(null);
        if(existingRouter == null) {
            return proxyRouterRepository.save(router);
        }
        if (router.getHost() != null)
            existingRouter.setHost(router.getHost());
        if (router.getMethod() != null)
            existingRouter.setMethod(router.getMethod());
        if (router.getPath() != null)
            existingRouter.setPath(router.getPath());
        if (router.getUri() != null)
            existingRouter.setUri(router.getUri());
        if (router.getHeaderName() != null)
            existingRouter.setHeaderName(router.getHeaderName());
        if (router.getHeaderValue() != null)
            existingRouter.setHeaderValue(router.getHeaderValue());
        if (router.getPathPattern() != null)
            existingRouter.setPathPattern(router.getPathPattern());
        if (router.getPathReplacement() != null)
            existingRouter.setPathReplacement(router.getPathReplacement());

        return proxyRouterRepository.save(existingRouter);
    }

    @Transactional
    public List<ProxyRouter> deleteByUserName(String userName) {
        List<ProxyRouter> routers =  proxyRouterRepository.deleteByUserName(userName);
        return routers;
    }

    @Transactional
    public List<ProxyRouter> deleteBySvcFullName(String svcFullName) {
        List<ProxyRouter> routers =  proxyRouterRepository.deleteBySvcFullName(svcFullName);
        return routers;
    }

    @Transactional
    public List<ProxyRouter> deleteBySvcFullNameAndPortNumber(String svcFullName, String port) {
        List<ProxyRouter> routers =  proxyRouterRepository.deleteBySvcFullNameAndPortNumber(svcFullName, port);
        return routers;
    }

    public ProxyRouter findById(ProxyRouterId routerId) {
        return proxyRouterRepository.findById(routerId).orElse(null);
    }

    public boolean existsById(ProxyRouterId routerId) {
        return proxyRouterRepository.existsById(routerId);
    }
}
