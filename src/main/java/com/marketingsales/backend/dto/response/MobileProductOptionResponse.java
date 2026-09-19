package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MobileProductOptionResponse {
    private Long id;
    private String sku;
    private String productName;
    private String displayLabel;
    private String uom;
    private Integer packSize;
    private BigDecimal basePrice;

    public static MobileProductOptionResponse from(Product product) {
        return MobileProductOptionResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .productName(product.getProductName())
                .displayLabel(product.getSku() + " - " + product.getProductName())
                .uom(product.getUom())
                .packSize(product.getPackSize())
                .basePrice(product.getBasePrice())
                .build();
    }
}
