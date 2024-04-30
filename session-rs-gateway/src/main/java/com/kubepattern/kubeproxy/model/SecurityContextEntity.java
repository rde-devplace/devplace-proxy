package com.kubepattern.kubeproxy.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "security_context")
public class SecurityContextEntity {

    @Id
    private String sessionId;
    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String authentication; // 인증 정보를 JSON 형태로 저장

    // getters and setters
}

