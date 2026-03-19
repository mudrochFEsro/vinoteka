package com.shopapi.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ProductNotFoundException.class,
            CategoryNotFoundException.class,
            CartNotFoundException.class,
            OrderNotFoundException.class
    })
    public ProblemDetail handleNotFound(RuntimeException ex) {
        var title = switch (ex) {
            case ProductNotFoundException e -> "Product Not Found";
            case CategoryNotFoundException e -> "Category Not Found";
            case CartNotFoundException e -> "Cart Not Found";
            case OrderNotFoundException e -> "Order Not Found";
            default -> "Not Found";
        };
        return createProblem(HttpStatus.NOT_FOUND, title, ex.getMessage());
    }

    @ExceptionHandler({InsufficientStockException.class, EmptyCartException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        var title = switch (ex) {
            case InsufficientStockException e -> "Insufficient Stock";
            case EmptyCartException e -> "Empty Cart";
            default -> "Bad Request";
        };
        return createProblem(HttpStatus.BAD_REQUEST, title, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        var problem = createProblem(HttpStatus.BAD_REQUEST, "Validation Error", "Validation failed");
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return createProblem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication required");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return createProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ProblemDetail createProblem(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
