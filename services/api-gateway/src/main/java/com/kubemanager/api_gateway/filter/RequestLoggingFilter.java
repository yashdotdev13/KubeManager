package com.kubemanager.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID);

        log.info(
                "Incoming Request | CorrelationId={} | Method={} | URI={} | ClientIP={}",
                correlationId,
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress()
        );

        return chain.filter(exchange)
                .doFinally(signal -> {

                    long executionTime = System.currentTimeMillis() - startTime;

                    log.info(
                            "Outgoing Response | CorrelationId={} | Status={} | Time={} ms",
                            correlationId,
                            exchange.getResponse().getStatusCode(),
                            executionTime
                    );
                });
    }
}