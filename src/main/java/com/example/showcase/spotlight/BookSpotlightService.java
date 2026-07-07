package com.example.showcase.spotlight;

import com.example.showcase.catalog.Book;
import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import com.example.showcase.catalog.BookRepository;
import com.example.showcase.order.OrderService;
import org.jspecify.annotations.Nullable;
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
                new SpotlightJoiner(),
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

    /**
     * Reimplements {@code Joiner.awaitAllSuccessfulOrThrow()}'s "cancel on first failure, throw
     * it from result()" behavior (that built-in joiner is package-private and final, so it
     * can't be extended), adding Java 26's {@code onTimeout()} hook (JEP 525). Without this
     * override, a timeout throws the unchecked {@link StructuredTaskScope.TimeoutException},
     * which isn't a {@link ResponseStatusException} and falls through {@link #adaptFailure} as
     * a generic 502; overriding it lets a timeout be reported as an accurate 504 instead.
     */
    private static final class SpotlightJoiner implements StructuredTaskScope.Joiner<Object, Void> {

        private volatile @Nullable Throwable firstException;
        private volatile boolean timedOut;

        @Override
        public boolean onComplete(StructuredTaskScope.Subtask<Object> subtask) {
            if (subtask.state() == StructuredTaskScope.Subtask.State.FAILED && firstException == null) {
                firstException = subtask.exception();
                return true;
            }
            return false;
        }

        @Override
        public void onTimeout() {
            timedOut = true;
        }

        @Override
        public Void result() throws Throwable {
            if (timedOut) {
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                        "Book spotlight assembly timed out before local and external lookups both completed");
            }
            if (firstException != null) {
                throw firstException;
            }
            return null;
        }
    }
}
