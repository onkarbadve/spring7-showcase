package com.example.showcase.spotlight;

import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses {@code DEFINED_PORT} rather than {@code RANDOM_PORT} - see
 * {@code BookControllerRestTestClientTests} for why (the app's self-referencing HTTP client
 * base URL depends on the fixed port 8080).
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class BookSpotlightControllerRestTestClientTests {

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:8080").build();
    }

    @Test
    void spotlightAssemblesLocalAndExternalDataForASeededBook() {
        long seededId = anySeededBookId();

        BookSpotlightResponse response = client.get().uri("/java26/books/{id}/spotlight", seededId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookSpotlightResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.requestContext()).isEqualTo("spotlight-book-" + seededId);
        assertThat(response.priceTier()).isNotBlank();
        assertThat(response.localBook().id()).isEqualTo(seededId);
        assertThat(response.externalCatalog()).isNotNull();
    }

    @Test
    void missingBookReturns404() {
        client.get().uri("/java26/books/999999/spotlight")
                .exchange()
                .expectStatus().isNotFound();
    }

    private long anySeededBookId() {
        List<BookSummaryV2> books = client.get().uri("/api/v2/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<BookSummaryV2>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(books).isNotEmpty();
        return books.getFirst().id();
    }
}
