package com.kubepattern.kubeproxy.repo;

import com.kubepattern.kubeproxy.model.ProxyRouter;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRouterRepository extends JpaRepository<ProxyRouter, String> {
    List<ProxyRouter> deleteByUserName(String userName);
}
