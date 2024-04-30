package com.kubepattern.kubeproxy.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OAuth2UserDetails {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private List<Authority> authorities;
}
