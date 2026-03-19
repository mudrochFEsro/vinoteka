package com.shopapi.backend.service;

import com.shopapi.backend.dto.CreateProductRequest;
import com.shopapi.backend.dto.ProductDTO;
import com.shopapi.backend.dto.UpdateProductRequest;
import com.shopapi.backend.entity.Product;
import com.shopapi.backend.exception.ProductNotFoundException;
import com.shopapi.backend.repository.CategoryRepository;
import com.shopapi.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> findAll(Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        var products = fetchProducts(categoryId, search, minPrice, maxPrice);

        return products.stream()
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> isBlank(search) || containsIgnoreCase(p.getName(), search))
                .map(ProductDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        return productRepository.findById(id)
                .map(ProductDTO::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductDTO create(CreateProductRequest request) {
        var product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(Objects.requireNonNullElse(request.stock(), 0))
                .imageUrl(request.imageUrl())
                .build();

        if (request.categoryId() != null) {
            categoryRepository.findById(request.categoryId())
                    .ifPresent(product::setCategory);
        }

        return ProductDTO.from(productRepository.save(product));
    }

    @Transactional
    public ProductDTO update(Long id, UpdateProductRequest request) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        updateIfNotNull(request.name(), product::setName);
        updateIfNotNull(request.description(), product::setDescription);
        updateIfNotNull(request.price(), product::setPrice);
        updateIfNotNull(request.stock(), product::setStock);
        updateIfNotNull(request.imageUrl(), product::setImageUrl);

        if (request.categoryId() != null) {
            categoryRepository.findById(request.categoryId())
                    .ifPresent(product::setCategory);
        }

        return ProductDTO.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private List<Product> fetchProducts(Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId);
        }
        if (!isBlank(search)) {
            return productRepository.findByNameContainingIgnoreCase(search);
        }
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetween(minPrice, maxPrice);
        }
        return productRepository.findAll();
    }

    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    private static boolean containsIgnoreCase(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    private static <T> void updateIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
