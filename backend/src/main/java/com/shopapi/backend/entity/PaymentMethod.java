package com.shopapi.backend.entity;

/**
 * Spôsob platby pri objednávke.
 *
 * CASH_ON_DELIVERY = Dobierka
 * - Packeta kuriér: hotovosť alebo karta pri prevzatí
 * - Packeta výdajné miesto: platba v Packeta appke kartou alebo hotovosť na mieste
 */
public enum PaymentMethod {
    CASH_ON_DELIVERY   // Dobierka (platba pri prevzatí)
}
