package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    @Size(min = 1, max = 200)
    private String productName;

    @Size(min = 1, max = 120)
    private String category;

    private Integer packSize;

    @Size(min = 1, max = 20)
    private String uom;

    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than zero")
    private BigDecimal mrp;
}
