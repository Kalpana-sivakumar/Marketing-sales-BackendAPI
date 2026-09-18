package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.ProductStatus;
import com.marketingsales.backend.dto.request.CreateProductRequest;
import com.marketingsales.backend.dto.request.UpdateProductRequest;
import com.marketingsales.backend.dto.response.ProductPageResponse;
import com.marketingsales.backend.dto.response.ProductResponse;
import com.marketingsales.backend.entity.Product;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.ProductRepository;
import com.marketingsales.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request, String actorName) {
        String sku = request.getSku().trim().toUpperCase();
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateResourceException("A product with this SKU already exists");
        }
        validatePackSize(request.getPackSize());
        validatePrice(request.getBasePrice(), request.getMrp());

        Product product = Product.builder()
                .sku(sku)
                .productName(request.getProductName().trim())
                .category(request.getCategory().trim())
                .packSize(request.getPackSize())
                .uom(request.getUom().trim().toUpperCase())
                .basePrice(request.getBasePrice())
                .mrp(request.getMrp())
                .status(request.getStatus() == null ? ProductStatus.ACTIVE : request.getStatus())
                .createdBy(actorName)
                .updatedBy(actorName)
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse findAll(String search, String category, String uom, Boolean active, int page, int size) {
        ProductStatus status = active == null ? null : (active ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);
        Page<Product> products = productRepository.searchProducts(
                normalize(search),
                normalize(category),
                normalize(uom),
                status,
                PageRequest.of(page, size, Sort.by("productName").ascending())
        );

        return ProductPageResponse.builder()
                .content(products.getContent().stream().map(ProductResponse::from).toList())
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request, String actorName) {
        Product product = getProduct(id);

        if (StringUtils.hasText(request.getProductName())) product.setProductName(request.getProductName().trim());
        if (StringUtils.hasText(request.getCategory())) product.setCategory(request.getCategory().trim());
        if (request.getPackSize() != null) {
            validatePackSize(request.getPackSize());
            product.setPackSize(request.getPackSize());
        }
        if (StringUtils.hasText(request.getUom())) product.setUom(request.getUom().trim().toUpperCase());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getMrp() != null) product.setMrp(request.getMrp());
        validatePrice(product.getBasePrice(), product.getMrp());

        product.setUpdatedBy(actorName);
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long id, boolean active, String actorName) {
        Product product = getProduct(id);
        product.setStatus(active ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);
        product.setUpdatedBy(actorName);
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void validatePackSize(Integer packSize) {
        if (packSize == null || packSize <= 0) {
            throw new BadRequestException("Pack size must be greater than zero");
        }
    }

    private void validatePrice(BigDecimal basePrice, BigDecimal mrp) {
        if (mrp.compareTo(basePrice) < 0) {
            throw new BadRequestException("MRP cannot be less than base price");
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
