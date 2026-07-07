package com.example.showcase.client;

import org.springframework.web.service.annotation.GetExchange;

/**
 * HTTP interface client (introduced in Spring Framework 6, streamlined in 7).
 * No implementation - Spring generates a proxy backed by RestClient at startup
 * and registers it as a bean (wiring is in {@code HttpServiceClientConfig}
 * via {@code @ImportHttpServices}).
 *
 * Points at a small in-app upstream stub by default so the sample remains
 * runnable without internet access. Swap the base URL and response type for a
 * real upstream service when moving past the demo.
 */
public interface CatalogLookupClient {

    @GetExchange("/posts/{id}")
    ExternalCatalogEntry findById(@org.springframework.web.bind.annotation.PathVariable("id") Long id);

    record ExternalCatalogEntry(long id, String title, String body) {
    }
}
