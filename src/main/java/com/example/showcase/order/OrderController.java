package com.example.showcase.order;

import com.example.showcase.client.CatalogLookupClient.ExternalCatalogEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * No version attribute here on purpose - unversioned endpoints keep working
     * alongside versioned ones; they simply don't participate in the version
     * resolution/matching.
     */
    @GetMapping("/external-lookup/{id}")
    public ExternalCatalogEntry externalLookup(@PathVariable("id") Long id) {
        return orderService.lookupExternalCatalogEntry(id);
    }
}
