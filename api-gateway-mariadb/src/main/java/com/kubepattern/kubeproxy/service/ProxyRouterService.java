package com.kubepattern.kubeproxy.service;

import java.util.List;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.repo.ProxyRouterRepository;

import jakarta.transaction.Transactional;

@Service
public class ProxyRouterService {

    private final ApplicationEventPublisher eventPublisher;
    private final ProxyRouterRepository proxyRouterRepository;

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

    public ProxyRouter save(ProxyRouter proxyRouter) {
        return proxyRouterRepository.save(proxyRouter);
    }

    public void deleteById(String path) {
        proxyRouterRepository.deleteById(path);
    }

    @Transactional
    public List<ProxyRouter> deleteByUserName(String userName) {
        List<ProxyRouter> routers =  proxyRouterRepository.deleteByUserName(userName);
        return routers;
    }

    public ProxyRouter findById(String path) {
        return proxyRouterRepository.findById(path).orElse(null);
    }

    public boolean existsById(String path) {
        return proxyRouterRepository.existsById(path);
    }
}
