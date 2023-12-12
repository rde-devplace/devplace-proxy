package com.kubepattern.kubeproxy.filters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * 사용자 특정 토큰을 처리하고 중계하는 사용자 정의 토큰 중계 필터를 나타냅니다.
 * 이 필터는 사용자 특정 세부 정보를 요청에 주입하기 위해 게이트웨이와 함께 사용할 수 있습니다.
 */
@Component
public class CustomTokenRelayFilter implements GlobalFilter, Ordered {

    /** 사용자의 값을 저장하기 위해 사용되는 헤더 */
    private final String USER_HEADER = "X-User-Value";

    /** 토큰 페이로드에서 사용자명을 추출하기 위해 사용되는 키 */
    private final String USER_ID = "preferred_username";

    /**
     * 들어오는 요청을 필터링하고 존재하는 경우 사용자의 값을 헤더로 주입합니다.
     * 이 메소드는 특히 bearer 토큰을 찾아 'preferred_username' 값을 추출합니다.
     *
     * @param exchange 현재 서버 측 HTTP 요청 또는 응답을 나타냅니다.
     * @param chain 체인의 다음 필터로 위임하는 방법을 제공합니다.
     * @return 요청 처리가 완료될 때를 나타내는 {@link Mono<Void>}.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String tokenValue = authHeader.substring(7);
            String[] chunks = tokenValue.split("\\.");

            Base64.Decoder decoder = Base64.getDecoder();
            //String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));

            String userName = getValue(payload, USER_ID);
            if (userName != null) {
                exchange.getRequest().mutate().header(USER_HEADER, userName).build();
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 주어진 JSON 문자열에서 특정 값을 추출합니다.
     *
     * @param json 입력 JSON 문자열입니다.
     * @param key 값이 추출되어야 하는 키입니다.
     * @return JSON 문자열에서 제공된 키와 연관된 값입니다.
     */
    public static String getValue(String json, String key) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        return jsonObject.get(key).getAsString();
    }

    /**
     * 필터 체인의 필터 실행에 대한 이 필터의 순서를 정의합니다.
     * 값이 낮은 필터는 우선 순위가 높습니다.
     *
     * @return 이 필터에 대한 순서 값입니다.
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
