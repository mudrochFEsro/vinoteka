package com.shopapi.backend.repository;

import com.shopapi.backend.entity.Category;
import com.shopapi.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * REPOSITORY = prístup k databáze (ako Prisma/Drizzle v JS svete)
 *
 * JpaRepository<Product, Long> znamená:
 * - Pracujem s entitou Product
 * - Primárny kľúč je typu Long
 *
 * MÁGIA Spring Data JPA:
 * Nemusíš písať SQL! Spring automaticky generuje SQL podľa názvu metódy:
 *
 * findByCategory       -> SELECT * FROM products WHERE category_id = ?
 * findByNameContaining -> SELECT * FROM products WHERE name LIKE '%?%'
 * findByPriceBetween   -> SELECT * FROM products WHERE price BETWEEN ? AND ?
 *
 * Toto je ako keby si mal v JS:
 * prisma.product.findMany({ where: { categoryId: 1 } })
 *
 * Zdarma dostaneš aj základné operácie:
 * - findAll()      -> SELECT * FROM products
 * - findById(id)   -> SELECT * FROM products WHERE id = ?
 * - save(product)  -> INSERT/UPDATE
 * - deleteById(id) -> DELETE FROM products WHERE id = ?
 * - existsById(id) -> SELECT EXISTS(...)
 * - count()        -> SELECT COUNT(*) FROM products
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Nájdi produkty podľa kategórie (celý objekt)
     * SQL: SELECT * FROM products WHERE category_id = ?
     */
    List<Product> findByCategory(Category category);

    /**
     * Nájdi produkty podľa ID kategórie
     * SQL: SELECT * FROM products WHERE category_id = ?
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Vyhľadaj produkty podľa názvu (case insensitive, obsahuje)
     * SQL: SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%search%')
     *
     * "Containing" = LIKE '%value%'
     * "IgnoreCase" = case insensitive
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Nájdi produkty v cenovom rozmedzí
     * SQL: SELECT * FROM products WHERE price BETWEEN minPrice AND maxPrice
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
