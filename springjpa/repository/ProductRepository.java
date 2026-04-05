package com.demo.springjpa.repository;

import com.demo.springjpa.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository showcases EVERY Spring Data JPA query technique:
 *
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  A. JpaRepository built-ins (CRUD + paging + sorting)   │
 *  │  B. Derived query methods (keyword-based)               │
 *  │  C. @Query – JPQL                                       │
 *  │  D. @Query – Native SQL                                 │
 *  │  E. Named queries (defined on the entity)               │
 *  │  F. @Modifying – update / delete queries                │
 *  └─────────────────────────────────────────────────────────┘
 *
 * Dynamic / Criteria-based queries are in ProductSpecification.java.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>
                                           {

    // ═══════════════════════════════════════════════════════════════════════
    // B.  DERIVED QUERY METHODS  (Spring Data generates the JPQL automatically)
    // ═══════════════════════════════════════════════════════════════════════

    // findBy + property name
    List<Product> findByCategory(String category);

    // findBy + Containing (LIKE %name%)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // findBy + boolean field
    List<Product> findByActiveTrue();
    List<Product> findByActiveFalse();

    // findBy + comparison
    List<Product> findByPriceLessThan(BigDecimal maxPrice);
    List<Product> findByPriceGreaterThanEqual(BigDecimal minPrice);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // findBy + AND
    List<Product> findByCategoryAndActiveTrue(String category);

    // findBy + OR
    List<Product> findByCategoryOrName(String category, String name);

    // findBy + OrderBy
    List<Product> findByCategoryOrderByPriceAsc(String category);
    List<Product> findAllByOrderByCreatedAtDesc();

    // countBy
   
    long countByActiveTrue();

    // existsBy
    boolean existsByName(String name);

    // deleteBy  (must be @Transactional at the call site or service layer)
    @Transactional
    void deleteByActiveFalse();

    // findFirst / findTop
    Optional<Product> findFirstByCategoryOrderByPriceAsc(String category);
    List<Product>     findTop3ByActiveTrueOrderByPriceDesc();

    // Paging support with derived queries
    Page<Product> findByCategory(String category, Pageable pageable);


    // ═══════════════════════════════════════════════════════════════════════
    // C.  @Query – JPQL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetch products whose name contains the given keyword (case-insensitive).
     * Uses positional parameter binding.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Product> searchByNameJpql(String keyword);

    /**
     * Fetch products in a price range with named parameters.
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max ORDER BY p.price")
    List<Product> findByPriceRange(@Param("min") BigDecimal min,
                                   @Param("max") BigDecimal max);

    /**
     * Projection – return only name and price as an Object[].
     */
    @Query("SELECT p.name, p.price FROM Product p WHERE p.category = :category")
    List<Object[]> findNameAndPriceByCategory(@Param("category") String category);

    /**
     * Aggregate – average price per category.
     */
    @Query("SELECT p.category, AVG(p.price) FROM Product p GROUP BY p.category")
    List<Object[]> findAveragePriceByCategory();


    // ═══════════════════════════════════════════════════════════════════════
    // D.  @Query – Native SQL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Native query: products with low stock (stock < threshold).
     */
    @Query(value = "SELECT * FROM products WHERE stock_quantity < :threshold AND active = true",
           nativeQuery = true)
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    /**
     * Native query with pagination.
     * When using nativeQuery + Pageable you must also supply a countQuery.
     */
    @Query(value     = "SELECT * FROM products WHERE category = :category",
           countQuery = "SELECT COUNT(*) FROM products WHERE category = :category",
           nativeQuery = true)
    Page<Product> findByCategoryNative(@Param("category") String category, Pageable pageable);


    // ═══════════════════════════════════════════════════════════════════════
    // E.  NAMED QUERIES  (defined with @NamedQuery on Product entity)
    //     Method names must match "<EntityName>.<queryName>" suffix
    // ═══════════════════════════════════════════════════════════════════════

    // Maps to @NamedQuery "Product.findByCategoryIgnoreCase"
    List<Product> findByCategoryIgnoreCase(String category);

    // Maps to @NamedQuery "Product.findActiveProductsUnderPrice"
    List<Product> findActiveProductsUnderPrice(@Param("maxPrice") BigDecimal maxPrice);

    // Maps to @NamedQuery "Product.countByCategory"
    Long countByCategory(@Param("category") String category);


    // ═══════════════════════════════════════════════════════════════════════
    // F.  @Modifying – UPDATE / DELETE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Bulk price increase for a category.
     * @Modifying + @Transactional required for DML statements.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = p.price * (1 + :pct / 100) WHERE p.category = :category")
    int increasePriceByCategory(@Param("category") String category,
                                @Param("pct")      BigDecimal pct);

    /**
     * Deactivate out-of-stock products.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.active = false WHERE p.stockQuantity = 0")
    int deactivateOutOfStockProducts();

    /**
     * Maps to @NamedQuery "Product.updateStock".
     */
    @Modifying
    @Transactional
    int updateStock(@Param("id") Long id, @Param("quantity") int quantity);
}
