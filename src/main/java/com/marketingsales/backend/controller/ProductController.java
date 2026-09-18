package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.CreateProductRequest;
import com.marketingsales.backend.dto.request.ProductStatusRequest;
import com.marketingsales.backend.dto.request.UpdateProductRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.ProductPageResponse;
import com.marketingsales.backend.dto.response.ProductResponse;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProductResponse response = productService.create(request, actorName(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @GetMapping
    public ApiResponse<ProductPageResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String uom,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(productService.findAll(search, category, uom, active, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(productService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("Product updated successfully", productService.update(id, request, actorName(principal)));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProductResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String message = request.getActive() ? "Product activated successfully" : "Product deactivated successfully";
        return ApiResponse.success(message, productService.updateStatus(id, request.getActive(), actorName(principal)));
    }

    private String actorName(UserPrincipal principal) {
        if (principal == null || principal.getFullName() == null || principal.getFullName().isBlank()) {
            return "Admin";
        }
        return principal.getFullName();
    }
}
