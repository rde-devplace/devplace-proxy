package com.kubepattern.kubeproxy.repo;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ProxyRouterRepository extends JpaRepository<ProxyRouter, String> {
    List<ProxyRouter> deleteByUserName(String userName);
}
