package com.kubepattern.kubeproxy.repo;

import com.kubepattern.kubeproxy.model.SecurityContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomSecurityContextRepository extends JpaRepository<SecurityContextEntity, String> {
    // 여기에 필요한 추가 메소드를 정의할 수 있습니다.
    SecurityContextEntity deleteBySessionId(String sessionId);
}

