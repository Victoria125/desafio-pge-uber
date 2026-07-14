package com.vitoria.apigateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${security.jwt.secret}") final String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        if (isPublicRequest(request)) {
            return chain.filter(exchange);
        }

        final Optional<String> token = resolveToken(request);
        if (token.isEmpty() || !isValid(token.get())) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicRequest(final ServerHttpRequest request) {
        final String path = request.getURI().getPath();
        final HttpMethod method = request.getMethod();

        return method == HttpMethod.OPTIONS
                || path.startsWith("/actuator")
                || (method == HttpMethod.POST && path.equals("/auth/login"))
                || (method == HttpMethod.POST && (path.equals("/accounts") || path.equals("/accounts/")));
    }

    private Optional<String> resolveToken(final ServerHttpRequest request) {
        final String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return Optional.of(authorization.substring(BEARER_PREFIX.length()).trim());
        }

        final String accessToken = request.getQueryParams().getFirst("access_token");
        if (hasText(accessToken)) {
            return Optional.of(accessToken);
        }

        return Optional.empty();
    }

    private boolean isValid(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(this.secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
