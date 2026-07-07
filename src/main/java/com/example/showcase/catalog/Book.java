package com.example.showcase.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Plain JPA 3.2 entity. Nothing exotic here on purpose - the point of the sample
 * is that with the package-level {@code @NullMarked}, {@code title} and
 * {@code price} are compiler-enforced non-null, while {@code isbn} is explicitly
 * optional via {@code @Nullable}. No Lombok, no builder ceremony - Java 25 records
 * are used elsewhere for DTOs; this stays a mutable entity because JPA still wants
 * a no-arg constructor and mutable state for the persistence context.
 */
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private @Nullable String isbn;

    @Positive
    private BigDecimal price;

    protected Book() {
        // required by JPA
        this.title = "";
        this.author = "";
        this.price = BigDecimal.ZERO;
    }

    public Book(String title, String author, @Nullable String isbn, BigDecimal price) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public @Nullable String getIsbn() {
        return isbn;
    }

    public void setIsbn(@Nullable String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
