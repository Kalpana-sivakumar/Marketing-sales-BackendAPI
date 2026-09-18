package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.CounterOrderPricingRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.CounterOrderPricingResponse;
import com.marketingsales.backend.dto.response.MobileProductOptionResponse;
import com.marketingsales.backend.dto.response.ProductPageResponse;
import com.marketingsales.backend.dto.response.ProductResponse;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/products")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('STAFF','ADMIN','MARKETING_MANAGER')")
public class MobileProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<ProductPageResponse> findAllForMobile(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String uom,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(productService.findAll(search, category, uom, true, page, size));
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<MobileProductOptionResponse>> dropdown(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(productService.findActiveProductOptions(search));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findByIdForMobile(@PathVariable Long id) {
        ProductResponse product = productService.findById(id);
        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BadRequestException("Product is inactive");
        }
        return ApiResponse.success(product);
    }

    @PostMapping("/order-pricing")
    public ApiResponse<CounterOrderPricingResponse> calculateOrderPricing(
            @Valid @RequestBody CounterOrderPricingRequest request
    ) {
        return ApiResponse.success(
                "Order total calculated successfully",
                productService.calculateCounterOrderPricing(request)
        );
    }
}
