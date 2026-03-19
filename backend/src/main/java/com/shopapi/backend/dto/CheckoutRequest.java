package com.shopapi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CheckoutRequest(
        // Contact
        @NotBlank(message = "Email je povinny")
        @Email(message = "Neplatny format emailu")
        String email,

        @NotBlank(message = "Meno je povinne")
        String firstName,

        @NotBlank(message = "Priezvisko je povinne")
        String lastName,

        String phone,

        // Address
        @NotBlank(message = "Ulica je povinna")
        String street,

        @NotBlank(message = "Cislo domu je povinne")
        String houseNumber,

        @NotBlank(message = "Mesto je povinne")
        String city,

        @NotBlank(message = "PSC je povinne")
        @Pattern(regexp = "\\d{3} ?\\d{2}", message = "PSC musi byt vo formate XXX XX")
        String postalCode,

        @NotBlank(message = "Krajina je povinna")
        @Size(min = 2, max = 2, message = "Krajina musi byt 2-znakovy kod")
        String country,

        // Company (optional)
        boolean isCompany,

        String companyName,

        String ico,

        String dic,

        String icDph,

        // Items - only for guest (auth users take from cart)
        @Valid
        List<CheckoutItem> items
) {
    public record CheckoutItem(
            @NotNull(message = "Product ID je povinne")
            Long productId,

            @NotNull(message = "Mnozstvo je povinne")
            @Positive(message = "Mnozstvo musi byt kladne")
            @Max(value = 99, message = "Maximalny pocet je 99")
            Integer quantity
    ) {}

    public String fullName() {
        return firstName + " " + lastName;
    }

    public String formattedAddress() {
        return "%s %s, %s %s, %s".formatted(street, houseNumber, postalCode, city, country);
    }
}
