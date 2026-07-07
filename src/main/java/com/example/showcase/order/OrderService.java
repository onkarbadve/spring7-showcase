package com.example.showcase.order;

import com.example.showcase.client.CatalogLookupClient;
import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Demonstrates the two core resilience annotations that shipped in Spring
 * Framework 7 (spring-core / spring-context, no external library):
 *
 * - {@code @Retryable}: retries on {@link RestClientException} (e.g. the
 *   upstream catalog service being flaky) with exponential backoff and jitter.
 * - {@code @ConcurrencyLimit}: caps how many callers can be inside this
 *   method at once, protecting the upstream service from a thundering herd -
 *   this matters more than ever with virtual threads, since there's no
 *   longer a thread-pool size implicitly capping concurrency.
 *
 * Both are enabled via {@code @EnableResilientMethods} on
 * {@code ShowcaseApplication}.
 */
@Service
public class OrderService {

    private final CatalogLookupClient catalogLookupClient;

    public OrderService(CatalogLookupClient catalogLookupClient) {
        this.catalogLookupClient = catalogLookupClient;
    }

    @Retryable(
            includes = RestClientException.class,
            maxRetries = 4,
            delay = 200,
            multiplier = 2.0,
            maxDelay = 2000,
            jitter = 100
    )
    @ConcurrencyLimit(5)
    public ExternalCatalogEntry lookupExternalCatalogEntry(long bookId) {
        return catalogLookupClient.findById(bookId);
    }
}
