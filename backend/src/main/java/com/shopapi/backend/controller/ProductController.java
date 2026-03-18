package com.shopapi.backend.controller;

import com.shopapi.backend.dto.CreateProductRequest;
import com.shopapi.backend.dto.ProductDTO;
import com.shopapi.backend.dto.UpdateProductRequest;
import com.shopapi.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * CONTROLLER = vstupný bod pre HTTP requesty (ako API routes v Next.js/SvelteKit)
 *
 * Tok dát:
 * Frontend (fetch) -> Controller -> Service -> Repository -> Databáza
 *
 * Controller len prijíma requesty a vracia odpovede,
 * biznis logika je v Service vrstve
 */
@RestController  // Označuje že všetky metódy vracajú JSON (nie HTML)
@RequestMapping("/api/products")  // Všetky endpointy začínajú /api/products
@RequiredArgsConstructor  // Lombok: vytvorí konštruktor pre final premenné (dependency injection)
@Tag(name = "Products", description = "Product management APIs")  // Pre Swagger dokumentáciu
public class ProductController {

    /**
     * Dependency Injection - Spring automaticky "vloží" ProductService
     * Nemusíš písať new ProductService(), Spring to spraví za teba
     */
    private final ProductService productService;

    /**
     * GET /api/products
     * GET /api/products?category=1&search=phone&minPrice=100&maxPrice=500
     *
     * Verejný endpoint - ktokoľvek môže čítať produkty
     *
     * @RequestParam = query parametre z URL (?category=1)
     * required = false znamená že parameter je nepovinný
     */
    @GetMapping  // HTTP GET metóda
    @Operation(summary = "Get all products", description = "Public endpoint with optional filtering")
    public List<ProductDTO> getAll(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return productService.findAll(category, search, minPrice, maxPrice);
    }

    /**
     * GET /api/products/123
     *
     * @PathVariable = hodnota z URL cesty (123 z /products/123)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Public endpoint")
    public ProductDTO getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    /**
     * POST /api/products
     * Body: { "name": "iPhone", "price": 999.99, ... }
     *
     * Len pre ADMIN (definované v SecurityConfig)
     *
     * @RequestBody = JSON z tela requestu sa zmapuje na CreateProductRequest
     * @Valid = skontroluje validačné pravidlá (@NotBlank, @Positive atď.)
     * @ResponseStatus(CREATED) = vráti HTTP 201 namiesto 200
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", description = "Admin only")
    public ProductDTO create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }

    /**
     * PUT /api/products/123
     * Body: { "name": "iPhone Pro", "price": 1099.99 }
     *
     * Aktualizuje existujúci produkt
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Admin only")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(id, request);
    }

    /**
     * DELETE /api/products/123
     *
     * Vymaže produkt
     * HTTP 204 No Content = úspech, ale bez odpovede
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", description = "Admin only")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
