package com.shopapi.backend.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot create order from empty cart");
    }
}
