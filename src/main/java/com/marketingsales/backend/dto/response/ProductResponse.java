package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String productName;
    private String category;
    private Integer packSize;
    private String uom;
    private BigDecimal basePrice;
    private BigDecimal mrp;
    private String status;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .productName(product.getProductName())
                .category(product.getCategory())
                .packSize(product.getPackSize())
                .uom(product.getUom())
                .basePrice(product.getBasePrice())
                .mrp(product.getMrp())
                .status(product.getStatus().name())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
