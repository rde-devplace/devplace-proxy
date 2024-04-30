package com.kubepattern.kubeproxy.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OAuth2AuthenticationDetails {
    private OAuth2UserDetails principal;
    private String authorizedClientRegistrationId;
    private List<Authority> authorities;
    private boolean authenticated;
}

