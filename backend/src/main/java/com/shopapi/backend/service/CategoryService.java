package com.shopapi.backend.service;

import com.shopapi.backend.dto.CategoryDTO;
import com.shopapi.backend.dto.CreateCategoryRequest;
import com.shopapi.backend.entity.Category;
import com.shopapi.backend.exception.CategoryNotFoundException;
import com.shopapi.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryDTO::from)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public CategoryDTO findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryDTO::from)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    @Transactional
    public CategoryDTO create(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .build();
        Category saved = categoryRepository.save(category);
        return CategoryDTO.from(saved);
    }

    @Transactional
    public CategoryDTO update(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setName(request.name());
        category.setSlug(request.slug());

        Category saved = categoryRepository.save(category);
        return CategoryDTO.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }
}
