package com.shopapi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTITY = trieda ktorá reprezentuje tabuľku v databáze
 * Každá inštancia (objekt) tejto triedy = jeden riadok v tabuľke "products"
 *
 * Analógia pre frontend developera:
 * - Entity je ako TypeScript interface, ale priamo prepojený s databázou
 * - Keď uložíš objekt, automaticky sa uloží do DB
 */
@Entity  // Označuje že toto je databázová tabuľka
@Table(name = "products")  // Názov tabuľky v PostgreSQL
@EntityListeners(AuditingEntityListener.class)  // Automaticky vypĺňa createdAt
@Data  // Lombok: vygeneruje gettery, settery, toString, equals, hashCode
@Builder  // Lombok: umožní vytvárať objekty cez Product.builder().name("...").build()
@NoArgsConstructor  // Lombok: vygeneruje prázdny konštruktor - new Product()
@AllArgsConstructor  // Lombok: vygeneruje konštruktor so všetkými parametrami
public class Product {

    /**
     * Primárny kľúč - unikátne ID každého produktu
     * IDENTITY = databáza sama generuje ID (1, 2, 3...)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Názov produktu - povinný (nullable = false)
     */
    @Column(nullable = false)
    private String name;

    /**
     * Popis produktu - nepovinný, môže byť dlhý text (TEXT typ v DB)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Cena produktu
     * precision = 10 znamená max 10 číslic celkom
     * scale = 2 znamená 2 desatinné miesta (napr. 99999999.99)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Počet kusov na sklade
     * @Builder.Default = ak neuvedieš hodnotu pri vytváraní, použije sa 0
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    /**
     * VZŤAH: Produkt patrí do jednej kategórie (ManyToOne)
     * Mnoho produktov -> jedna kategória
     *
     * LAZY = kategória sa načíta z DB až keď ju naozaj potrebuješ
     * (šetrí výkon - ako lazy loading obrázkov na webe)
     *
     * JoinColumn = v tabuľke products bude stĺpec "category_id"
     * ktorý odkazuje na tabuľku categories
     */
    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Dátum vytvorenia - automaticky sa nastaví pri uložení
     * updatable = false znamená že sa už nikdy nezmení
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
