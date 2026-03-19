package com.shopapi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record GuestOrderRequest(
        @NotBlank(message = "Email je povinny")
        @Email(message = "Neplatny format emailu")
        String email,

        @NotBlank(message = "Meno je povinne")
        String firstName,

        @NotBlank(message = "Priezvisko je povinne")
        String lastName,

        @NotBlank(message = "Ulica je povinna")
        String street,

        @NotBlank(message = "Cislo domu je povinne")
        String houseNumber,

        @NotBlank(message = "Mesto je povinne")
        String city,

        @NotBlank(message = "PSC je povinne")
        String postalCode,

        @NotBlank(message = "Krajina je povinna")
        String country,

        @NotEmpty(message = "Kosik je prazdny")
        @Valid
        List<GuestOrderItem> items
) {
    public record GuestOrderItem(
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
        return """
                %s %s, %s %s, %s %s, %s""".formatted(
                firstName, lastName, street, houseNumber, postalCode, city, country
        );
    }
}
