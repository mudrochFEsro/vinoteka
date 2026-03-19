package com.shopapi.backend.dto;

import com.shopapi.backend.entity.DeliveryMethod;
import com.shopapi.backend.entity.PaymentMethod;
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

        // Address (required for COURIER/PACKETA_COURIER, optional for PACKETA_PICKUP)
        String street,

        String houseNumber,

        String city,

        String postalCode,

        @Size(min = 2, max = 2, message = "Krajina musi byt 2-znakovy kod")
        String country,

        // Company (optional)
        boolean isCompany,

        String companyName,

        String ico,

        String dic,

        String icDph,

        // Delivery
        @NotNull(message = "Sposob dopravy je povinny")
        DeliveryMethod deliveryMethod,

        // Packeta pickup point (required for PACKETA_PICKUP)
        String packetaPointId,

        String packetaPointName,

        // Payment
        @NotNull(message = "Sposob platby je povinny")
        PaymentMethod paymentMethod,

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
