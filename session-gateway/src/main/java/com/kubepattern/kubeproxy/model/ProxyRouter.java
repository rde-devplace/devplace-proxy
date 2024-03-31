package com.kubepattern.kubeproxy.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@Table(name = "proxy_router_1", indexes = @Index(name = "idx_username", columnList = "userName"))
@IdClass(ProxyRouterId.class) // 복합 키 클래스를 지정
public class ProxyRouter {

    @Id
    private String path;
    @Id
    private String svcFullName; // New field
    @Id
    private String portNumber;

    private String uri;
    private String method;
    private String host;
    private String headerName;
    private String headerValue;
    private String pathPattern; // New field for RewritePath filter's pattern (e.g. "/api/v1/users/(?<segment>.*)")
    private String pathReplacement; // New field for RewritePath filter's replacement value (e.g. "/todo/${segment}")
    @ColumnDefault("false")
    private boolean tokenRelay; // true if TokenRelay filter should be applied, false otherwise
    private String userName;

    public ProxyRouterId getId() {
        return new ProxyRouterId(svcFullName, portNumber, path);
    }
}


