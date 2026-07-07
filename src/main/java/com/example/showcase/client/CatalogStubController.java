package com.example.showcase.client;

import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo-upstream")
public class CatalogStubController {

    @GetMapping("/posts/{id}")
    public ExternalCatalogEntry postById(@PathVariable("id") Long id) {
        return new ExternalCatalogEntry(
                id,
                "Catalog note for book " + id,
                "Synthetic upstream payload served locally to exercise Spring HTTP interfaces."
        );
    }
}
