package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CounterOrderPricingResponse {
    private List<PricedItem> items;
    private BigDecimal totalPrice;

    @Getter
    @Builder
    public static class PricedItem {
        private Long productId;
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
