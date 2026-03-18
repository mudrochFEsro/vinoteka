package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Category;

public record CategoryDTO(
        Long id,
        String name,
        String slug,
        int productCount
) {
    public static CategoryDTO from(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getProducts() != null ? category.getProducts().size() : 0
        );
    }
}
