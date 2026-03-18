package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO = Data Transfer Object
 *
 * Účel: Definuje AKÉ DÁTA posielame cez API (response)
 *
 * Prečo nie priamo Entity?
 * 1. Bezpečnosť - nechceš poslať všetko (napr. heslá)
 * 2. Formát - môžeš dáta transformovať (napr. categoryName namiesto celého objektu)
 * 3. Stabilita API - entity sa môže zmeniť, ale DTO ostane rovnaké
 *
 * "record" = Java 16+ skratka pre immutable dátovú triedu
 * Automaticky generuje: konštruktor, gettery, equals, hashCode, toString
 *
 * Toto je ekvivalent v TypeScripte:
 * interface ProductDTO {
 *   id: number;
 *   name: string;
 *   description: string;
 *   price: number;
 *   stock: number;
 *   categoryId: number | null;
 *   categoryName: string | null;
 *   createdAt: string;
 * }
 */
public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {
    /**
     * Factory metóda - vytvorí DTO z Entity
     *
     * Použitie: ProductDTO.from(product)
     *
     * Transformuje databázový objekt na API response
     */
    public static ProductDTO from(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCreatedAt()
        );
    }
}
