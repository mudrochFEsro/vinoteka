package com.shopapi.backend.service;

import com.shopapi.backend.dto.CreateProductRequest;
import com.shopapi.backend.dto.ProductDTO;
import com.shopapi.backend.dto.UpdateProductRequest;
import com.shopapi.backend.entity.Category;
import com.shopapi.backend.entity.Product;
import com.shopapi.backend.exception.ProductNotFoundException;
import com.shopapi.backend.repository.CategoryRepository;
import com.shopapi.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * SERVICE = biznis logika aplikácie
 *
 * Tu píšeš "čo aplikácia robí":
 * - validácie, výpočty, kombinovanie dát z viacerých zdrojov
 *
 * Analógia: Service je ako utils/helpers v JS, ale organizované okolo domény
 * ProductService = všetko čo súvisí s produktmi
 */
@Service  // Spring vie že toto je service a môže ho "injektovať" inde
@RequiredArgsConstructor  // Dependency injection cez konštruktor
public class ProductService {

    // Repository = prístup k databáze (ako Prisma/Drizzle v JS)
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Nájdi všetky produkty s voliteľným filtrovaním
     *
     * @Transactional(readOnly = true) =
     *   - Všetky DB operácie v tejto metóde sú v jednej transakcii
     *   - readOnly = optimalizácia, hovoríme DB že len čítame
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll(Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products;

        // Načítaj produkty podľa filtrov
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(search);
        } else if (minPrice != null && maxPrice != null) {
            products = productRepository.findByPriceBetween(minPrice, maxPrice);
        } else {
            products = productRepository.findAll();
        }

        // Stream API = ako .filter().map() v JavaScripte
        // Aplikuj dodatočné filtre a transformuj na DTO
        return products.stream()
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> search == null || search.isBlank() ||
                        p.getName().toLowerCase().contains(search.toLowerCase()))
                .map(ProductDTO::from)  // Transformuj Entity -> DTO
                .toList();
    }

    /**
     * Nájdi produkt podľa ID
     *
     * Optional = môže obsahovať hodnotu alebo byť prázdny (ako null v JS)
     * orElseThrow = ak je prázdny, vyhoď výnimku (error)
     */
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        return productRepository.findById(id)
                .map(ProductDTO::from)  // Ak existuje, transformuj na DTO
                .orElseThrow(() -> new ProductNotFoundException(id));  // Ak nie, error 404
    }

    /**
     * Vytvor nový produkt
     *
     * Builder pattern = elegantný spôsob vytvárania objektov
     * Namiesto: new Product(); p.setName("x"); p.setPrice(10);
     * Píšeš:    Product.builder().name("x").price(10).build();
     */
    @Transactional  // Zapisujeme do DB, potrebujeme transakciu
    public ProductDTO create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock() != null ? request.stock() : 0)
                .imageUrl(request.imageUrl())
                .build();

        // Ak bol zadaný categoryId, nájdi kategóriu a priraď ju
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElse(null);
            product.setCategory(category);
        }

        // save() uloží do databázy a vráti uložený objekt (s vygenerovaným ID)
        Product saved = productRepository.save(product);
        return ProductDTO.from(saved);
    }

    /**
     * Aktualizuj existujúci produkt (PATCH/PUT)
     *
     * Aktualizujeme len tie polia, ktoré boli poslané (nie null)
     */
    @Transactional
    public ProductDTO update(Long id, UpdateProductRequest request) {
        // Najprv nájdi produkt, ak neexistuje -> 404
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Aktualizuj len polia ktoré prišli (partial update)
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElse(null);
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        return ProductDTO.from(saved);
    }

    /**
     * Vymaž produkt
     */
    @Transactional
    public void delete(Long id) {
        // Over že produkt existuje
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
