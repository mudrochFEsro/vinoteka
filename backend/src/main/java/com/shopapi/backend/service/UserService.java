package com.shopapi.backend.service;

import com.shopapi.backend.entity.User;
import com.shopapi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public String getCurrentKeycloakId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String subject = jwt.getSubject();
        if (subject == null) {
            subject = jwt.getClaimAsString("sub");
            if (subject == null) {
                subject = jwt.getClaimAsString("preferred_username");
            }
            if (subject == null) {
                subject = jwt.getId();
            }
        }

        if (subject == null) {
            log.error("Could not extract user ID from JWT. Available claims: {}", jwt.getClaims().keySet());
            throw new org.springframework.security.access.AccessDeniedException("Could not identify user from token");
        }

        return subject;
    }

    public String getCurrentEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("email");
    }

    public String getCurrentName() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = jwt.getClaimAsString("name");
        if (name == null) {
            name = jwt.getClaimAsString("preferred_username");
        }
        return name;
    }

    @Transactional
    public User getOrCreateCurrentUser() {
        String keycloakId = getCurrentKeycloakId();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakId(keycloakId)
                            .email(getCurrentEmail())
                            .name(getCurrentName())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    /**
     * Returns current authenticated user if present, empty Optional otherwise.
     * Used for endpoints that work with both authenticated and guest users.
     */
    @Transactional
    public Optional<User> getCurrentUserOptional() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return Optional.empty();
        }

        try {
            return Optional.of(getOrCreateCurrentUser());
        } catch (Exception e) {
            log.debug("Could not get current user: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
