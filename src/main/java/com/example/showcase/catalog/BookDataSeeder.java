package com.example.showcase.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookDataSeeder implements CommandLineRunner {

    private final BookRepository repository;

    public BookDataSeeder(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.save(new Book("Effective Java", "Joshua Bloch", "978-0134685991", new BigDecimal("45.99")));
        repository.save(new Book("Spring in Action", "Craig Walls", null, new BigDecimal("39.99")));
    }
}
