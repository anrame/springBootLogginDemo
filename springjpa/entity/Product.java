package com.demo.springjpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity demonstrating:
 *  - Basic JPA mappings
 *  - @NamedQuery  – single named query
 *  - @NamedQueries – multiple named queries bundled together
 *
 * Named queries are compiled at application startup (fail-fast on JPQL errors).
 */
@NamedQueries({

    // ── Named Query 1: find by category (case-insensitive) ───────────────────
    @NamedQuery(
        name  = "Product.findByCategoryIgnoreCase",
        query = "SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category)"
    ),

    // ── Named Query 2: find active products below a price ceiling ─────────────
    @NamedQuery(
        name  = "Product.findActiveProductsUnderPrice",
        query = "SELECT p FROM Product p " +
                "WHERE p.active = true AND p.price < :maxPrice " +
                "ORDER BY p.price ASC"
    ),

    // ── Named Query 3: count products per category ────────────────────────────
    @NamedQuery(
        name  = "Product.countByCategory",
        query = "SELECT COUNT(p) FROM Product p WHERE p.category = :category"
    ),

    // ── Named Query 4: update stock for a given product ───────────────────────
    @NamedQuery(
        name  = "Product.updateStock",
        query = "UPDATE Product p SET p.stockQuantity = :quantity WHERE p.id = :id"
    )
})
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name must not be blank")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Category must not be blank")
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
