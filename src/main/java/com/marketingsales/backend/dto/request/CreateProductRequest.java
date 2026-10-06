package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String productName;

    @NotBlank(message = "Category is required")
    @Size(max = 120)
    private String category;

    @NotNull(message = "Pack size is required")
    private Integer packSize;

    @NotBlank(message = "UOM is required")
    @Size(max = 20)
    private String uom;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than zero")
    private BigDecimal mrp;

    private ProductStatus status;
}
