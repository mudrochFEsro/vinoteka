package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;

    @NotBlank(message = "Ulica je povinná")
    private String street;

    @NotBlank(message = "Číslo domu je povinné")
    private String houseNumber;

    @NotBlank(message = "Mesto je povinné")
    private String city;

    @NotBlank(message = "PSČ je povinné")
    @Pattern(regexp = "^\\d{3} ?\\d{2}$", message = "PSČ musí byť vo formáte XXX XX")
    private String postalCode;

    @NotNull(message = "Krajina je povinná")
    private Country country;

    private boolean isDefault;
}
