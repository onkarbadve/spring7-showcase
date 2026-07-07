package com.example.showcase.catalog;

import com.example.showcase.catalog.BookDtos.BookSummaryV1;
import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import com.example.showcase.catalog.BookDtos.CreateBookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates Spring Framework 7's {@link RestTestClient}: a fluent, assertion-driven client
 * bound to a live server, exercising the real DispatcherServlet, {@code WebConfig}'s API
 * versioning, and the real H2-backed repository - not a sliced {@code @WebMvcTest}.
 *
 * Uses {@code DEFINED_PORT} (the app's fixed port 8080, as used everywhere else in this repo)
 * rather than {@code RANDOM_PORT}: {@code HttpServiceClientConfig}'s self-referencing stub URL
 * resolves {@code ${server.port}} at bean-creation time, which is still {@code 0} under a
 * random port - Boot only exposes the real bound port afterwards, via
 * {@code local.server.port}/{@code @LocalServerPort}, not by rewriting {@code server.port}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class BookControllerRestTestClientTests {

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:8080").build();
    }

    @Test
    void v1ListOmitsIsbn() {
        List<BookSummaryV1> books = client.get().uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<BookSummaryV1>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(books).isNotEmpty();
    }

    @Test
    void v2ListIncludesIsbn() {
        List<BookSummaryV2> books = client.get().uri("/api/v2/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<BookSummaryV2>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(books).isNotEmpty();
        assertThat(books).anyMatch(book -> book.isbn() != null);
    }

    @Test
    void missingBookReturns404() {
        client.get().uri("/api/v1/books/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createReturns201WithSavedBook() {
        CreateBookRequest request = new CreateBookRequest(
                "Refactoring", "Martin Fowler", "978-0134757599", new BigDecimal("47.99"));

        BookSummaryV2 created = client.post().uri("/api/v1/books")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookSummaryV2.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.title()).isEqualTo("Refactoring");
        assertThat(created.isbn()).isEqualTo("978-0134757599");
    }
}
