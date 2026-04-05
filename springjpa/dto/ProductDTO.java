package com.demo.springjpa.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Request / Response DTO – keeps the API contract separate from the entity. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    private boolean active = true;
}
