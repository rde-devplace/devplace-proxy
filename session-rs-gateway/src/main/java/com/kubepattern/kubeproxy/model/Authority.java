package com.kubepattern.kubeproxy.model;

import lombok.Data;

import java.util.Map;

@Data
public class Authority {
    private String authority;
    private Map<String, Object> attributes;
}
