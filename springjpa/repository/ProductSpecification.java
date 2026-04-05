package com.demo.springjpa.repository;

import com.demo.springjpa.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * ProductSpecification – Dynamic / Criteria-API queries.
 *
 * Each static method returns a {@link Specification<Product>} predicate that
 * can be combined with {@code and()} / {@code or()} at runtime.
 *
 * Usage example:
 * <pre>
 *   Specification<Product> spec = Specification
 *       .where(ProductSpecification.hasCategory("Electronics"))
 *       .and(ProductSpecification.isActive())
 *       .and(ProductSpecification.priceBetween(new BigDecimal("10"), new BigDecimal("500")));
 *
 *   List<Product> results = productRepository.findAll(spec);
 * </pre>
 *
 * The repository must extend {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}.
 */
public final class ProductSpecification {

    private ProductSpecification() {}  // utility class

    /** Filter by exact category match. */
    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) ->
            category == null ? null
                : cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    /** Filter only active products. */
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    /** Filter only inactive products. */
    public static Specification<Product> isInactive() {
        return (root, query, cb) -> cb.isFalse(root.get("active"));
    }

    /** Filter products whose name contains the given keyword (case-insensitive). */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) ->
            keyword == null ? null
                : cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    /** Filter products with price >= minPrice. */
    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
            minPrice == null ? null
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /** Filter products with price <= maxPrice. */
    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
            maxPrice == null ? null
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /** Filter products within a price range. */
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    /** Filter products with stockQuantity > 0. */
    public static Specification<Product> inStock() {
        return (root, query, cb) ->
            cb.greaterThan(root.get("stockQuantity"), 0);
    }

    /** Filter products with stockQuantity below a threshold (low-stock alert). */
    public static Specification<Product> lowStock(int threshold) {
        return (root, query, cb) ->
            cb.lessThan(root.get("stockQuantity"), threshold);
    }
}
