package com.demo.springjpa.controller;

import com.demo.springjpa.dto.ProductDTO;
import com.demo.springjpa.dto.ProductSearchCriteria;
import com.demo.springjpa.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST API exposing all query features.
 *
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │  Base URL: http://localhost:8080/api/products                            │
 * │  H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:jpadb) │
 * └──────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ════════════════════════════════════════════════════════════════════════
    // CRUD endpoints
    // ════════════════════════════════════════════════════════════════════════

    /** POST /api/products  → create */
    @PostMapping
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /** GET /api/products/{id} → findById */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * GET /api/products?page=0&size=5&sortBy=price&direction=asc
     * → paginated list of all products
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "asc")  String direction) {
        return ResponseEntity.ok(service.findAll(page, size, sortBy, direction));
    }

    /** PUT /api/products/{id} → full update */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /** PATCH /api/products/{id} → partial update */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDTO> partialUpdate(@PathVariable Long id,
                                                    @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.partialUpdate(id, dto));
    }

    /** DELETE /api/products/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Derived query method endpoints
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/products/by-category/{category} */
    @GetMapping("/by-category/{category}")
    public List<ProductDTO> byCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }

    /** GET /api/products/search?keyword=laptop */
    @GetMapping("/search")
    public List<ProductDTO> search(@RequestParam String keyword) {
        return service.findByNameKeyword(keyword);
    }

    /** GET /api/products/active */
    @GetMapping("/active")
    public List<ProductDTO> active() {
        return service.findActive();
    }

    /** GET /api/products/inactive */
    @GetMapping("/inactive")
    public List<ProductDTO> inactive() {
        return service.findInactive();
    }

    /** GET /api/products/price-range?min=10&max=100 */
    @GetMapping("/price-range")
    public List<ProductDTO> priceRange(@RequestParam BigDecimal min,
                                       @RequestParam BigDecimal max) {
        return service.findByPriceRange(min, max);
    }

    /** GET /api/products/top3-expensive */
    @GetMapping("/top3-expensive")
    public List<ProductDTO> top3Expensive() {
        return service.findTop3MostExpensiveActive();
    }

    /** GET /api/products/by-category/{category}/paged?page=0&size=3 */
    @GetMapping("/by-category/{category}/paged")
    public Page<ProductDTO> byCategoryPaged(@PathVariable String category,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "5") int size) {
        return service.findByCategoryPaged(category, page, size);
    }

    /** GET /api/products/count-by-category/{category} */
    @GetMapping("/count-by-category/{category}")
    public Map<String, Object> countByCategory(@PathVariable String category) {
        return Map.of("category", category, "count", service.countByCategory(category));
    }

    // ════════════════════════════════════════════════════════════════════════
    // @Query (JPQL & Native) endpoints
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/products/jpql-search?keyword=book */
    @GetMapping("/jpql-search")
    public List<ProductDTO> jpqlSearch(@RequestParam String keyword) {
        return service.searchByNameJpql(keyword);
    }

    /** GET /api/products/avg-price-by-category */
    @GetMapping("/avg-price-by-category")
    public List<Map<String, Object>> avgPriceByCategory() {
        return service.getAveragePriceByCategory();
    }

    /** GET /api/products/name-price/{category} */
    @GetMapping("/name-price/{category}")
    public List<Map<String, Object>> namePriceByCategory(@PathVariable String category) {
        return service.getNameAndPriceByCategory(category);
    }

    /** GET /api/products/low-stock?threshold=20   (Native SQL) */
    @GetMapping("/low-stock")
    public List<ProductDTO> lowStock(@RequestParam(defaultValue = "20") int threshold) {
        return service.getLowStockProducts(threshold);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Named Query endpoints
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/products/named/by-category/{category} */
    @GetMapping("/named/by-category/{category}")
    public List<ProductDTO> namedByCategory(@PathVariable String category) {
        return service.findByCategoryNamedQuery(category);
    }

    /** GET /api/products/named/under-price?maxPrice=100 */
    @GetMapping("/named/under-price")
    public List<ProductDTO> namedUnderPrice(@RequestParam BigDecimal maxPrice) {
        return service.findActiveUnderPrice(maxPrice);
    }

    /** PATCH /api/products/{id}/stock?quantity=99 */
    @PatchMapping("/{id}/stock")
    public Map<String, Object> updateStock(@PathVariable Long id,
                                           @RequestParam int quantity) {
        int updated = service.updateStock(id, quantity);
        return Map.of("updatedRows", updated);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Dynamic / Specification endpoints
    // ════════════════════════════════════════════════════════════════════════



    // ════════════════════════════════════════════════════════════════════════
    // Bulk / @Modifying endpoints
    // ════════════════════════════════════════════════════════════════════════

    /** POST /api/products/increase-price/{category}?pct=10 */
    @PostMapping("/increase-price/{category}")
    public Map<String, Object> increasePrice(@PathVariable String category,
                                             @RequestParam BigDecimal pct) {
        return Map.of("updatedRows", service.increasePriceByCategory(category, pct));
    }

    /** POST /api/products/deactivate-out-of-stock */
    @PostMapping("/deactivate-out-of-stock")
    public Map<String, Object> deactivateOutOfStock() {
        return Map.of("updatedRows", service.deactivateOutOfStock());
    }

    /** DELETE /api/products/inactive-all */
    @DeleteMapping("/inactive-all")
    public ResponseEntity<Void> deleteInactive() {
        service.deleteInactive();
        return ResponseEntity.noContent().build();
    }
}
