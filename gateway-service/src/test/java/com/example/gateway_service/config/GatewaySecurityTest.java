package com.example.gateway_service.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

    @LocalServerPort
    int gatewayPort;

    static DisposableServer downstream;

    @BeforeAll
    static void startDownstream() {
        downstream = HttpServer.create()
                .host("localhost")
                .port(0) // random free port
                .route(routes -> routes.get("/ok", (req, res) -> res.sendString(reactor.core.publisher.Mono.just("OK"))))
                .bindNow();
    }

    @AfterAll
    static void stopDownstream() {
        if (downstream != null) downstream.disposeNow();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        // Basic Auth user for Spring Security (adjust if you configured differently)
        r.add("spring.security.user.name", () -> "demo");
        r.add("spring.security.user.password", () -> "demo");

        // Define a single in-test route that proxies to our local downstream server
        r.add("spring.cloud.gateway.routes[0].id", () -> "test-route");
        r.add("spring.cloud.gateway.routes[0].uri", () -> "http://localhost:" + downstream.port());
        r.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/test/**");
        r.add("spring.cloud.gateway.routes[0].filters[0]", () -> "RewritePath=/test/(?<segment>.*), /${segment}");
    }

    private WebTestClient client() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + gatewayPort)
                .build();
    }

    @Test
    void withoutAuth_shouldReturn401_beforeProxying() {
        client()
                .get().uri("/test/ok")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void withBasicAuth_shouldProxy_andReturn200() {
        client()
                .get().uri("/test/ok")
                .headers(h -> h.setBasicAuth("demo", "demo"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).isEqualTo("OK"));
    }
}