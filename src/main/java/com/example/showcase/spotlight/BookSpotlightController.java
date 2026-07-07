package com.example.showcase.spotlight;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/java26")
public class BookSpotlightController {

    private final BookSpotlightService bookSpotlightService;

    public BookSpotlightController(BookSpotlightService bookSpotlightService) {
        this.bookSpotlightService = bookSpotlightService;
    }

    @GetMapping("/books/{id}/spotlight")
    public BookSpotlightResponse spotlight(@PathVariable("id") Long id) {
        return bookSpotlightService.spotlight(id);
    }
}
