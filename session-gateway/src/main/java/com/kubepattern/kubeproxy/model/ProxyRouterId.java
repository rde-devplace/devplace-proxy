package com.kubepattern.kubeproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ProxyRouterId implements Serializable {
    private String path;
    private String portNumber;
    private String svcFullName; // New field

    // 모든 필드를 포함하는 생성자
    public ProxyRouterId(String svcFullName, String portNumber, String path) {
        this.path = path;
        this.portNumber = portNumber;
        this.svcFullName = svcFullName;
    }

    // getter, setter (생략 가능, Lombok @Data 사용 가능)
    // equals와 hashCode 메소드 (생략 가능, Lombok @EqualsAndHashCode 사용 가능)
}
