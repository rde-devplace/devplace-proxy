package com.kubepattern.kubeproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@SpringBootApplication
@EnableWebFluxSecurity
public class KubeProxyRenewApplication {

    public static void main(String[] args) {
        SpringApplication.run(KubeProxyRenewApplication.class, args);
    }

}
