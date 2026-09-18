package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.request.CreateProductRequest;
import com.marketingsales.backend.dto.request.UpdateProductRequest;
import com.marketingsales.backend.dto.response.ProductPageResponse;
import com.marketingsales.backend.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse create(CreateProductRequest request, String actorName);
    ProductPageResponse findAll(String search, String category, String uom, Boolean active, int page, int size);
    ProductResponse findById(Long id);
    ProductResponse update(Long id, UpdateProductRequest request, String actorName);
    ProductResponse updateStatus(Long id, boolean active, String actorName);
}
