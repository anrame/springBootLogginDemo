package com.demo.springjpa.service;

import com.demo.springjpa.dto.ProductDTO;
import com.demo.springjpa.dto.ProductSearchCriteria;
import com.demo.springjpa.entity.Product;
import com.demo.springjpa.repository.ProductRepository;
import com.demo.springjpa.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer that demonstrates every JPA querying technique:
 *
 *  1.  Basic CRUD  (save / findById / findAll / delete)
 *  2.  Derived query methods
 *  3.  @Query (JPQL & Native SQL)
 *  4.  Named queries
 *  5.  Dynamic queries via Specification (Criteria API)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repo;

    // ════════════════════════════════════════════════════════════════════════
    // 1.  BASIC CRUD
    // ════════════════════════════════════════════════════════════════════════

    /** CREATE */
    @Transactional
    public ProductDTO create(ProductDTO dto) {
        if (repo.existsByName(dto.getName())) {
            throw new IllegalArgumentException("A product named '" + dto.getName() + "' already exists.");
        }
        Product saved = repo.save(toEntity(dto));
        log.info("Created product id={}", saved.getId());
        return toDTO(saved);
    }

    /** READ – single */
    public ProductDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    /** READ – all (with paging) */
    public Page<ProductDTO> findAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.findAll(pageable).map(this::toDTO);
    }

    /** READ – all (no paging) */
    public List<ProductDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    /** UPDATE – full replace */
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        Product existing = getOrThrow(id);
        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setPrice(dto.getPrice());
        existing.setStockQuantity(dto.getStockQuantity());
        existing.setActive(dto.isActive());
        Product saved = repo.save(existing);   // save() acts as merge when entity has an id
        log.info("Updated product id={}", saved.getId());
        return toDTO(saved);
    }

    /** PARTIAL UPDATE (PATCH) */
    @Transactional
    public ProductDTO partialUpdate(Long id, ProductDTO dto) {
        Product existing = getOrThrow(id);
        if (dto.getName()          != null) existing.setName(dto.getName());
        if (dto.getCategory()      != null) existing.setCategory(dto.getCategory());
        if (dto.getPrice()         != null) existing.setPrice(dto.getPrice());
        if (dto.getStockQuantity() != null) existing.setStockQuantity(dto.getStockQuantity());
        return toDTO(repo.save(existing));
    }

    /** DELETE */
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        repo.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    /** DELETE ALL */
    @Transactional
    public void deleteAll() {
        repo.deleteAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2.  DERIVED QUERY METHODS
    // ════════════════════════════════════════════════════════════════════════

    public List<ProductDTO> findByCategory(String category) {
        return map(repo.findByCategory(category));
    }

    public List<ProductDTO> findByNameKeyword(String keyword) {
        return map(repo.findByNameContainingIgnoreCase(keyword));
    }

    public List<ProductDTO> findActive() {
        return map(repo.findByActiveTrue());
    }

    public List<ProductDTO> findInactive() {
        return map(repo.findByActiveFalse());
    }

    public List<ProductDTO> findByPriceRange(BigDecimal min, BigDecimal max) {
        return map(repo.findByPriceBetween(min, max));
    }

    public List<ProductDTO> findActiveByCategoryOrderedByPrice(String category) {
        return map(repo.findByCategoryAndActiveTrue(category));
    }

    public List<ProductDTO> findTop3MostExpensiveActive() {
        return map(repo.findTop3ByActiveTrueOrderByPriceDesc());
    }

    public Page<ProductDTO> findByCategoryPaged(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return repo.findByCategory(category, pageable).map(this::toDTO);
    }

    public long countByCategory(String category) {
        return repo.countByCategory(category);
    }

    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3.  @Query – JPQL
    // ════════════════════════════════════════════════════════════════════════

    public List<ProductDTO> searchByNameJpql(String keyword) {
        return map(repo.searchByNameJpql(keyword));
    }

    public List<ProductDTO> findByPriceRangeJpql(BigDecimal min, BigDecimal max) {
        return map(repo.findByPriceRange(min, max));
    }

    /** Returns a list of {name, price} projections. */
    public List<Map<String, Object>> getNameAndPriceByCategory(String category) {
        return repo.findNameAndPriceByCategory(category).stream()
                   .map(row -> Map.of("name", row[0], "price", row[1]))
                   .collect(Collectors.toList());
    }

    /** Returns average price grouped by category. */
    public List<Map<String, Object>> getAveragePriceByCategory() {
        return repo.findAveragePriceByCategory().stream()
                   .map(row -> Map.of("category", row[0], "averagePrice", row[1]))
                   .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3b.  @Query – Native SQL
    // ════════════════════════════════════════════════════════════════════════

    public List<ProductDTO> getLowStockProducts(int threshold) {
        return map(repo.findLowStockProducts(threshold));
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4.  NAMED QUERIES  (defined with @NamedQuery on the entity)
    // ════════════════════════════════════════════════════════════════════════

    /** Uses @NamedQuery "Product.findByCategoryIgnoreCase" */
    public List<ProductDTO> findByCategoryNamedQuery(String category) {
        return map(repo.findByCategoryIgnoreCase(category));
    }

    /** Uses @NamedQuery "Product.findActiveProductsUnderPrice" */
    public List<ProductDTO> findActiveUnderPrice(BigDecimal maxPrice) {
        return map(repo.findActiveProductsUnderPrice(maxPrice));
    }

    /** Uses @NamedQuery "Product.updateStock" */
    @Transactional
    public int updateStock(Long id, int quantity) {
        getOrThrow(id);   // validate existence first
        return repo.updateStock(id, quantity);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5.  DYNAMIC QUERIES (Criteria API / Specification)
    // ════════════════════════════════════════════════════════════════════════

   

  

    private Specification<Product> buildSpec(ProductSearchCriteria c) {
        Specification<Product> spec = Specification.where(null);

        if (c.getName()     != null) spec = spec.and(ProductSpecification.nameContains(c.getName()));
        if (c.getCategory() != null) spec = spec.and(ProductSpecification.hasCategory(c.getCategory()));
        if (c.getMinPrice() != null) spec = spec.and(ProductSpecification.priceGreaterThanOrEqual(c.getMinPrice()));
        if (c.getMaxPrice() != null) spec = spec.and(ProductSpecification.priceLessThanOrEqual(c.getMaxPrice()));

        if (Boolean.TRUE.equals(c.getActive()))   spec = spec.and(ProductSpecification.isActive());
        if (Boolean.FALSE.equals(c.getActive()))  spec = spec.and(ProductSpecification.isInactive());

        if (Boolean.TRUE.equals(c.getInStock()))  spec = spec.and(ProductSpecification.inStock());
        if (c.getLowStockThreshold() != null)     spec = spec.and(ProductSpecification.lowStock(c.getLowStockThreshold()));

        return spec;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6.  BULK @Modifying QUERIES
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public int increasePriceByCategory(String category, BigDecimal pct) {
        return repo.increasePriceByCategory(category, pct);
    }

    @Transactional
    public int deactivateOutOfStock() {
        return repo.deactivateOutOfStockProducts();
    }

    @Transactional
    public void deleteInactive() {
        repo.deleteByActiveFalse();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════════

    private Product getOrThrow(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    private List<ProductDTO> map(List<Product> products) {
        return products.stream().map(this::toDTO).toList();
    }

    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                         .id(p.getId())
                         .name(p.getName())
                         .category(p.getCategory())
                         .price(p.getPrice())
                         .stockQuantity(p.getStockQuantity())
                         .active(p.isActive())
                         .build();
    }

    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                      .name(dto.getName())
                      .category(dto.getCategory())
                      .price(dto.getPrice())
                      .stockQuantity(dto.getStockQuantity())
                      .active(dto.isActive())
                      .build();
    }
}
