package com.example.showcase.order;

import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses {@code DEFINED_PORT} rather than {@code RANDOM_PORT} - see
 * {@code BookControllerRestTestClientTests} for why (the app's self-referencing HTTP client
 * base URL depends on the fixed port 8080).
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class OrderControllerRestTestClientTests {

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:8080").build();
    }

    @Test
    void externalLookupReturnsStubbedUpstreamEntry() {
        ExternalCatalogEntry entry = client.get().uri("/orders/external-lookup/42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExternalCatalogEntry.class)
                .returnResult()
                .getResponseBody();

        assertThat(entry).isNotNull();
        assertThat(entry.id()).isEqualTo(42L);
        assertThat(entry.title()).contains("42");
    }

    @Test
    void unversionedOrdersPathIsUnaffectedByApiVersioning() {
        client.get().uri("/orders/external-lookup/1")
                .exchange()
                .expectStatus().isOk();
    }
}
