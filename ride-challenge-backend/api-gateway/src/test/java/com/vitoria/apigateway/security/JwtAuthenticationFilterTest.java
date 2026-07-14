package com.vitoria.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String SECRET = "ride-challenge-jwt-secret-key-32-bytes-minimum-value";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET);

    @Mock
    private GatewayFilterChain chain;

    @Test
    void givenARequestWithoutToken_whenCallsFilter_thenReturnsUnauthorized() {
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("/rides").build());

        this.filter.filter(exchange, this.chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(this.chain);
    }

    @Test
    void givenARequestWithAnInvalidToken_whenCallsFilter_thenReturnsUnauthorized() {
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("/rides")
                        .header("Authorization", "Bearer invalid-token")
                        .build());

        this.filter.filter(exchange, this.chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(this.chain);
    }

    @Test
    void givenARequestWithAnExpiredToken_whenCallsFilter_thenReturnsUnauthorized() {
        final String expiredToken = token("user-1", "CLIENT", Instant.now().minusSeconds(60));
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("/rides")
                        .header("Authorization", "Bearer " + expiredToken)
                        .build());

        this.filter.filter(exchange, this.chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(this.chain);
    }

    @Test
    void givenARequestWithAValidToken_whenCallsFilter_thenForwardsIdentityHeaders() {
        final String aToken = token("user-1", "CLIENT", Instant.now().plusSeconds(300));
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.post("/rides")
                        .header("Authorization", "Bearer " + aToken)
                        .build());
        when(this.chain.filter(any())).thenReturn(Mono.empty());

        this.filter.filter(exchange, this.chain).block();

        final ServerWebExchange forwarded = capturedExchange();
        assertEquals("user-1", forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_ID_HEADER));
        assertEquals("CLIENT", forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_TYPE_HEADER));
    }

    @Test
    void givenARequestWithSpoofedIdentityHeaders_whenCallsFilter_thenReplacesThemWithTokenIdentity() {
        final String aToken = token("driver-1", "DRIVER", Instant.now().plusSeconds(300));
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.post("/rides/1/accept")
                        .header("Authorization", "Bearer " + aToken)
                        .header(JwtAuthenticationFilter.USER_ID_HEADER, "someone-else")
                        .header(JwtAuthenticationFilter.USER_TYPE_HEADER, "CLIENT")
                        .build());
        when(this.chain.filter(any())).thenReturn(Mono.empty());

        this.filter.filter(exchange, this.chain).block();

        final ServerWebExchange forwarded = capturedExchange();
        assertEquals(1, forwarded.getRequest().getHeaders().get(JwtAuthenticationFilter.USER_ID_HEADER).size());
        assertEquals("driver-1", forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_ID_HEADER));
        assertEquals("DRIVER", forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_TYPE_HEADER));
    }

    @Test
    void givenARequestWithATokenInQueryParam_whenCallsFilter_thenForwardsIdentityHeaders() {
        final String aToken = token("driver-1", "DRIVER", Instant.now().plusSeconds(300));
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("/ws?access_token=" + aToken).build());
        when(this.chain.filter(any())).thenReturn(Mono.empty());

        this.filter.filter(exchange, this.chain).block();

        final ServerWebExchange forwarded = capturedExchange();
        assertEquals("driver-1", forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_ID_HEADER));
    }

    @Test
    void givenAPublicRequestWithoutToken_whenCallsFilter_thenPassesThroughWithoutIdentityHeaders() {
        final MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.post("/auth/login")
                        .header(JwtAuthenticationFilter.USER_ID_HEADER, "spoofed")
                        .build());
        when(this.chain.filter(any())).thenReturn(Mono.empty());

        this.filter.filter(exchange, this.chain).block();

        final ServerWebExchange forwarded = capturedExchange();
        assertNull(forwarded.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.USER_ID_HEADER));
        assertNull(exchange.getResponse().getStatusCode());
    }

    private ServerWebExchange capturedExchange() {
        final ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(this.chain, times(1)).filter(captor.capture());
        return captor.getValue();
    }

    private static String token(final String subject, final String type, final Instant expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(expiration))
                .signWith(KEY)
                .compact();
    }
}
