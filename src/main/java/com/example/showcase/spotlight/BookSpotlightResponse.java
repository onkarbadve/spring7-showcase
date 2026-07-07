package com.example.showcase.spotlight;

import com.example.showcase.catalog.BookDtos.BookSummaryV2;
import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;

public record BookSpotlightResponse(
        String requestContext,
        String priceTier,
        BookSummaryV2 localBook,
        ExternalCatalogEntry externalCatalog
) {
}
