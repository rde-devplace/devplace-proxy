package com.kubepattern.kubeproxy.repo;

import com.kubepattern.kubeproxy.model.ProxyRouter;
import com.kubepattern.kubeproxy.model.ProxyRouterId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ProxyRouterRepository extends JpaRepository<ProxyRouter, ProxyRouterId> {
    List<ProxyRouter> deleteByUserName(String userName);
    List<ProxyRouter> deleteBySvcFullName(String svcFullName);
    List<ProxyRouter> deleteBySvcFullNameAndPortNumber(String svcFullName, String portNumber);
    List<ProxyRouter> findAllBySvcFullNameAndPortNumber(String svcFullName, String portNumber);
}
