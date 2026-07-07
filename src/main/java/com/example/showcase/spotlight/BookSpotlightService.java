package com.example.showcase.spotlight;

import com.example.showcase.catalog.Book;
import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import com.example.showcase.catalog.BookRepository;
import com.example.showcase.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.ScopedValue;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

/**
 * Demonstrates Java 26 structured concurrency plus scoped values in an
 * application-style use case: assemble one response from local persistence and
 * an upstream HTTP dependency while carrying request-local context without
 * thread-locals.
 */
@Service
public class BookSpotlightService {

    private static final ScopedValue<String> REQUEST_CONTEXT = ScopedValue.newInstance();

    private final BookRepository bookRepository;
    private final OrderService orderService;

    public BookSpotlightService(BookRepository bookRepository, OrderService orderService) {
        this.bookRepository = bookRepository;
        this.orderService = orderService;
    }

    public BookSpotlightResponse spotlight(long bookId) {
        return ScopedValue.where(REQUEST_CONTEXT, "spotlight-book-" + bookId).call(() -> assembleSpotlight(bookId));
    }

    private BookSpotlightResponse assembleSpotlight(long bookId) {
        try (var scope = StructuredTaskScope.<Object, Void>open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow(),
                config -> config
                        .withName("book-spotlight")
                        .withTimeout(Duration.ofSeconds(3))
                        .withThreadFactory(Thread.ofVirtual().name("spotlight-", 0).factory())
        )) {
            var localBookTask = scope.fork(() -> findBook(bookId));
            var externalCatalogTask = scope.fork(() -> orderService.lookupExternalCatalogEntry(bookId));

            scope.join();

            Book book = (Book) localBookTask.get();
            return new BookSpotlightResponse(
                    REQUEST_CONTEXT.get(),
                    classifyPrice(book.getPrice()),
                    toBookSummary(book),
                    externalCatalogTask.get()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Interrupted while assembling spotlight for book " + bookId, ex);
        } catch (RuntimeException ex) {
            throw adaptFailure(bookId, ex);
        }
    }

    private Book findBook(long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + bookId + " not found"));
    }

    private String classifyPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.valueOf(20)) < 0) {
            return "budget";
        }
        if (price.compareTo(BigDecimal.valueOf(40)) < 0) {
            return "standard";
        }
        if (price.compareTo(BigDecimal.valueOf(60)) < 0) {
            return "premium";
        }
        return "collector";
    }

    private BookSummaryV2 toBookSummary(Book book) {
        return new BookSummaryV2(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPrice());
    }

    private ResponseStatusException adaptFailure(long bookId, RuntimeException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ResponseStatusException responseStatusException) {
                return responseStatusException;
            }
            current = current.getCause();
        }
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Failed to assemble spotlight for book " + bookId, ex);
    }
}
