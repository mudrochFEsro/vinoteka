package com.shopapi.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapi.backend.dto.CreateProductRequest;
import com.shopapi.backend.entity.Category;
import com.shopapi.backend.entity.Product;
import com.shopapi.backend.repository.CategoryRepository;
import com.shopapi.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = categoryRepository.save(
                Category.builder()
                        .name("Cervene vina")
                        .slug("cervene-vina")
                        .build()
        );

        testProduct = productRepository.save(
                Product.builder()
                        .name("Frankovka modra")
                        .description("Kvalitne cervene vino")
                        .price(BigDecimal.valueOf(12.99))
                        .stock(50)
                        .category(testCategory)
                        .build()
        );
    }

    @Test
    @DisplayName("GET /api/products - should return all products")
    void getAllProducts_returnsAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Frankovka modra")))
                .andExpect(jsonPath("$[0].categoryName", is("Cervene vina")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return product by id")
    void getProductById_returnsProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Frankovka modra")))
                .andExpect(jsonPath("$.price", is(12.99)));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return 404 for non-existent product")
    void getProductById_returns404ForNonExistent() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Product Not Found")));
    }

    @Test
    @DisplayName("GET /api/products?categoryId - should filter by category")
    void getProducts_filtersByCategory() throws Exception {
        Category otherCategory = categoryRepository.save(
                Category.builder().name("Biele vina").slug("biele-vina").build()
        );
        productRepository.save(
                Product.builder()
                        .name("Rizling")
                        .description("Biele vino")
                        .price(BigDecimal.valueOf(9.99))
                        .stock(30)
                        .category(otherCategory)
                        .build()
        );

        mockMvc.perform(get("/api/products").param("category", testCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Frankovka modra")));
    }

    @Test
    @DisplayName("POST /api/products - should require ADMIN role")
    void createProduct_requiresAdminRole() throws Exception {
        CreateProductRequest newProduct = new CreateProductRequest(
                "New Wine", "Description", BigDecimal.valueOf(15.99),
                20, null, testCategory.getId()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/products - should deny access for USER role")
    void createProduct_deniedForUserRole() throws Exception {
        CreateProductRequest newProduct = new CreateProductRequest(
                "New Wine", "Description", BigDecimal.valueOf(15.99),
                20, null, testCategory.getId()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products - should create product for ADMIN")
    void createProduct_createsProductForAdmin() throws Exception {
        CreateProductRequest newProduct = new CreateProductRequest(
                "New Wine", "A new wine", BigDecimal.valueOf(15.99),
                20, null, testCategory.getId()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Wine")))
                .andExpect(jsonPath("$.price", is(15.99)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/products/{id} - should update product")
    void updateProduct_updatesProduct() throws Exception {
        CreateProductRequest updateData = new CreateProductRequest(
                "Updated Wine", "Updated description", BigDecimal.valueOf(18.99),
                100, null, testCategory.getId()
        );

        mockMvc.perform(put("/api/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Wine")))
                .andExpect(jsonPath("$.price", is(18.99)))
                .andExpect(jsonPath("$.stock", is(100)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} - should delete product")
    void deleteProduct_deletesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNotFound());
    }
}
