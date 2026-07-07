package com.example.showcase.spotlight;

import com.example.showcase.catalog.Book;
import com.example.showcase.catalog.BookRepository;
import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;
import com.example.showcase.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookSpotlightServiceTests {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final OrderService orderService = mock(OrderService.class);
    private final BookSpotlightService service = new BookSpotlightService(bookRepository, orderService);

    @Test
    void assemblesLocalAndExternalDataIntoOneResponse() {
        Book book = new Book("Domain-Driven Design", "Eric Evans", "9780321125217", BigDecimal.valueOf(54.99));
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(orderService.lookupExternalCatalogEntry(7L))
                .thenReturn(new ExternalCatalogEntry(7L, "External summary", "Remote description"));

        BookSpotlightResponse response = service.spotlight(7L);

        assertEquals("spotlight-book-7", response.requestContext());
        assertEquals("premium", response.priceTier());
        assertEquals("Domain-Driven Design", response.localBook().title());
        assertEquals("External summary", response.externalCatalog().title());
    }

    @Test
    void returnsNotFoundWhenTheLocalBookIsMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        when(orderService.lookupExternalCatalogEntry(99L))
                .thenReturn(new ExternalCatalogEntry(99L, "Unused", "Unused"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.spotlight(99L));

        assertEquals(404, exception.getStatusCode().value());
    }
}
