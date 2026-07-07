package com.example.showcase.catalog;

import com.example.showcase.catalog.BookDtos.BookSummaryV1;
import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import com.example.showcase.catalog.BookDtos.CreateBookRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Gatherers;

/**
 * Demonstrates Spring Framework 7's native API versioning support
 * (see {@code WebConfig} for the resolution strategy - here, path-segment based,
 * e.g. GET /api/v1/books vs GET /api/v2/books).
 *
 * Same URL path, same method name space, two response shapes selected purely by
 * the {@code version} attribute on {@code @GetMapping} - no manual header
 * parsing, no separate controller classes per version.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping(version = "1")
    public List<BookSummaryV1> listV1() {
        return repository.findAll().stream().map(BookSummaryV1::from).toList();
    }

    @GetMapping(version = "2")
    public List<BookSummaryV2> listV2() {
        return repository.findAll().stream().map(BookSummaryV2::from).toList();
    }

    @GetMapping(value = "/{id}", version = "1")
    public BookSummaryV1 getV1(@PathVariable Long id) {
        return BookSummaryV1.from(findOrThrow(id));
    }

    @GetMapping(value = "/{id}", version = "2")
    public BookSummaryV2 getV2(@PathVariable Long id) {
        return BookSummaryV2.from(findOrThrow(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookSummaryV2 create(@Valid @RequestBody CreateBookRequest request) {
        Book book = new Book(request.title(), request.author(), request.isbn(), request.price());
        return BookSummaryV2.from(repository.save(book));
    }

    /**
     * Demonstrates Java 24's Stream Gatherers (JEP 485): {@code Gatherers.windowFixed} chunks
     * the catalog into fixed-size sublists in one intermediate stream operation - no manual
     * index bookkeeping or a separate collector needed.
     */
    @GetMapping("/batches")
    public List<List<BookSummaryV2>> batches(@RequestParam(defaultValue = "2") int size) {
        return repository.findAll().stream()
                .map(BookSummaryV2::from)
                .gather(Gatherers.windowFixed(size))
                .toList();
    }

    private Book findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + id + " not found"));
    }
}
