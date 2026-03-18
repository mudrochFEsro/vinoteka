package com.shopapi.backend.controller;

import com.shopapi.backend.dto.CategoryDTO;
import com.shopapi.backend.dto.CreateCategoryRequest;
import com.shopapi.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Public endpoint - returns all categories")
    public List<CategoryDTO> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Public endpoint")
    public CategoryDTO getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Public endpoint")
    public CategoryDTO getBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category", description = "Admin only")
    @SecurityRequirement(name = "bearerAuth")
    public CategoryDTO create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Admin only")
    @SecurityRequirement(name = "bearerAuth")
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category", description = "Admin only")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
