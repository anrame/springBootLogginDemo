package com.demo.springjpa.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Carries the optional filter parameters for a dynamic search.
 * Any field left null is simply ignored when building the Specification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductSearchCriteria {

    private String  name;        // partial match
    private String  category;    // exact match (case-insensitive)
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean active;
    private Boolean inStock;     // stock > 0
    private Integer lowStockThreshold;  // stock < threshold
}
