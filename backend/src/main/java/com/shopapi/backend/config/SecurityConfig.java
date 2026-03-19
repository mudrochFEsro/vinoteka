package com.shopapi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SECURITY CONFIG = konfigurácia autentifikácie a autorizácie
 *
 * Autentifikácia = "Kto si?" (overenie identity cez JWT token)
 * Autorizácia = "Čo môžeš robiť?" (kontrola rolí USER/ADMIN)
 *
 * Analógia pre frontend:
 * - Toto je ako middleware v Next.js/SvelteKit ktorý chráni routes
 * - Ale na úrovni backendu
 */
@Configuration  // Konfiguračná trieda - Spring ju načíta pri štarte
@EnableWebSecurity  // Zapne Spring Security
@EnableMethodSecurity  // Umožní @PreAuthorize anotácie na metódach
public class SecurityConfig {

    /**
     * @Bean = metóda ktorá vytvára objekt spravovaný Springom
     * Spring ho vytvorí raz a potom ho používa všade kde treba
     *
     * SecurityFilterChain = reťaz filtrov cez ktoré prejde každý HTTP request
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS - povoľ requesty z frontendu (iná doména/port)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF - vypni pre REST API (používame JWT tokeny namiesto cookies)
            .csrf(csrf -> csrf.disable())

            // STATELESS = server si nepamätá session, každý request musí mať token
            // (oproti tradičným webom kde server drží session v pamäti)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // AUTORIZÁCIA - ktoré endpointy kto môže volať
            .authorizeHttpRequests(auth -> auth
                // === VEREJNÉ ENDPOINTY (bez prihlásenia) ===
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // GET requesty na produkty a kategórie sú verejné
                // (ktokoľvek môže prezerať produkty)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()

                // Guest orders (bez prihlásenia)
                .requestMatchers(HttpMethod.POST, "/api/orders/guest").permitAll()
                // Unified checkout (works for both auth and guest)
                .requestMatchers(HttpMethod.POST, "/api/orders/checkout").permitAll()

                // === ADMIN ONLY ENDPOINTY ===
                // Len používateľ s rolou ADMIN môže:
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/files/**").hasRole("ADMIN")
                // - vytvárať, upravovať, mazať produkty
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                // - vytvárať, upravovať, mazať kategórie
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                // === PRIHLÁSENÍ POUŽÍVATELIA ===
                // Všetko ostatné vyžaduje rolu USER alebo ADMIN
                // (napr. košík, objednávky)
                .anyRequest().hasAnyRole("USER", "ADMIN")
            )

            // JWT AUTENTIFIKÁCIA - overuj tokeny cez Keycloak
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * CORS konfigurácia - povoľ cross-origin requesty z frontendu
     *
     * Bez tohto by browser blokoval requesty z localhost:3000 na localhost:8081
     * (Same-Origin Policy)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Povolené origin-y (odkiaľ môžu prísť requesty)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*"  // All localhost ports for dev
        ));

        // Povolené HTTP metódy
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Povolené headery (vrátane Authorization pre JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Povoľ posielanie cookies/credentials
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Aplikuj na všetky cesty
        return source;
    }

    /**
     * JWT Converter - extrahuje informácie z JWT tokenu
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Konverter rolí z Keycloak formátu do Spring Security formátu
     *
     * Keycloak posiela role v JWT takto:
     * {
     *   "realm_access": {
     *     "roles": ["USER", "ADMIN"]
     *   }
     * }
     *
     * Spring Security očakáva role s prefixom "ROLE_":
     * ["ROLE_USER", "ROLE_ADMIN"]
     *
     * Tento konverter robí túto transformáciu
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Vytiahni realm_access claim z JWT
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();  // Žiadne role
            }

            // Vytiahni zoznam rolí
            List<String> roles = (List<String>) realmAccess.get("roles");

            // Transformuj ["USER", "ADMIN"] -> ["ROLE_USER", "ROLE_ADMIN"]
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        }
    }
}
