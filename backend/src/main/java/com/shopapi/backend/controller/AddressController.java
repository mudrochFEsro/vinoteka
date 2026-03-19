package com.shopapi.backend.controller;

import com.shopapi.backend.dto.AddressDTO;
import com.shopapi.backend.entity.Address;
import com.shopapi.backend.entity.User;
import com.shopapi.backend.repository.AddressRepository;
import com.shopapi.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @GetMapping
    public List<AddressDTO> getMyAddresses() {
        User user = userService.getOrCreateCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody AddressDTO dto
    ) {
        User user = userService.getOrCreateCurrentUser();

        // If this is the first address or marked as default, update others
        if (dto.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .street(dto.getStreet())
                .houseNumber(dto.getHouseNumber())
                .city(dto.getCity())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .isDefault(dto.isDefault() || addressRepository.findByUserId(user.getId()).isEmpty())
                .build();

        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO dto
    ) {
        User user = userService.getOrCreateCurrentUser();

        return addressRepository.findByIdAndUserId(id, user.getId())
                .map(address -> {
                    if (dto.isDefault() && !address.isDefault()) {
                        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                                .ifPresent(addr -> {
                                    addr.setDefault(false);
                                    addressRepository.save(addr);
                                });
                    }

                    address.setStreet(dto.getStreet());
                    address.setHouseNumber(dto.getHouseNumber());
                    address.setCity(dto.getCity());
                    address.setPostalCode(dto.getPostalCode());
                    address.setCountry(dto.getCountry());
                    address.setDefault(dto.isDefault());

                    return ResponseEntity.ok(toDTO(addressRepository.save(address)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id
    ) {
        User user = userService.getOrCreateCurrentUser();

        return addressRepository.findByIdAndUserId(id, user.getId())
                .map(address -> {
                    boolean wasDefault = address.isDefault();
                    addressRepository.delete(address);

                    // If deleted address was default, make another one default
                    if (wasDefault) {
                        addressRepository.findByUserId(user.getId()).stream()
                                .findFirst()
                                .ifPresent(addr -> {
                                    addr.setDefault(true);
                                    addressRepository.save(addr);
                                });
                    }

                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private AddressDTO toDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .houseNumber(address.getHouseNumber())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }
}
