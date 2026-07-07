package com.example.showcase.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * DTOs kept as records - separate from the entity so the API versioning demo
 * below can evolve the response shape (v1 vs v2) independently of persistence.
 */
public final class BookDtos {

    private BookDtos() {
    }

    /** v1 response shape: title/author/price only. */
    public record BookSummaryV1(Long id, String title, String author, BigDecimal price) {

        static BookSummaryV1 from(Book book) {
            return new BookSummaryV1(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice());
        }
    }

    /** v2 response shape: adds isbn. This is the field v1 clients never asked for. */
    public record BookSummaryV2(Long id, String title, String author, @Nullable String isbn, BigDecimal price) {

        static BookSummaryV2 from(Book book) {
            return new BookSummaryV2(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPrice());
        }
    }

    public record CreateBookRequest(
            @NotBlank String title,
            @NotBlank String author,
            @Nullable String isbn,
            @Positive BigDecimal price) {
    }
}
