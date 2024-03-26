package com.kubepattern.kubeproxy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@Table(name = "proxy_router_1", indexes = @Index(name = "idx_username", columnList = "userName"))
public class ProxyRouter {

    @Id
    private String path;
    private String uri;
    private String method;
    private String host;
    private String headerName;
    private String headerValue;
    private String pathPattern; // New field for RewritePath filter's pattern (e.g. "/api/v1/users/(?<segment>.*)")
    private String pathReplacement; // New field for RewritePath filter's replacement value (e.g. "/todo/${segment}")
    @ColumnDefault("false")
    private boolean tokenRelay; // true if TokenRelay filter should be applied, false otherwise
    private String userName; // New field
}


