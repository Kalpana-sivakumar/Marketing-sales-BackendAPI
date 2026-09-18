package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.ProductStatus;
import com.marketingsales.backend.dto.request.CounterOrderPricingRequest;
import com.marketingsales.backend.dto.response.CounterOrderPricingResponse;
import com.marketingsales.backend.dto.response.MobileProductOptionResponse;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<MobileProductOptionResponse> findActiveProductOptions(String search) {
        String normalized = normalize(search);
        return productRepository.findByStatusOrderByProductNameAsc(ProductStatus.ACTIVE).stream()
                .filter(product -> normalized == null
                        || product.getSku().toLowerCase().contains(normalized.toLowerCase())
                        || product.getProductName().toLowerCase().contains(normalized.toLowerCase()))
                .map(MobileProductOptionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CounterOrderPricingResponse calculateCounterOrderPricing(CounterOrderPricingRequest request) {
        Map<Long, Integer> quantitiesByProduct = mergeByProduct(request.getItems());
        List<Long> productIds = new ArrayList<>(quantitiesByProduct.keySet());
        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (productMap.size() != quantitiesByProduct.size()) {
            throw new BadRequestException("One or more selected products do not exist");
        }

        List<CounterOrderPricingResponse.PricedItem> pricedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
            Product product = productMap.get(entry.getKey());
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException("Inactive products cannot be added to orders");
            }
            BigDecimal lineTotal = product.getBasePrice().multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(lineTotal);

            pricedItems.add(CounterOrderPricingResponse.PricedItem.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .productName(product.getProductName())
                    .quantity(entry.getValue())
                    .unitPrice(product.getBasePrice())
                    .lineTotal(lineTotal)
                    .build());
        }

        return CounterOrderPricingResponse.builder()
                .items(pricedItems)
                .totalPrice(total)
                .build();
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

    private Map<Long, Integer> mergeByProduct(List<CounterOrderPricingRequest.LineItem> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CounterOrderPricingRequest.LineItem item : items) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Quantity must be at least 1");
            }
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }
}
