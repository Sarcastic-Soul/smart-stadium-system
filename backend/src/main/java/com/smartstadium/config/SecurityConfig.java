package com.smartstadium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration providing request validation and response hardening.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment env;

    private boolean isMockSecurity() {
        return !java.util.Arrays.asList(env.getActiveProfiles()).contains(
            "cloud"
        );
    }

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value(
        "${spring.security.oauth2.resourceserver.jwt.audiences:smart-stadium-api}"
    )
    private String audience;

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable()) // CSRF disabled: stateless API consumed by non-browser clients or tokens stored in memory
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                )
            )
            .headers(headers ->
                headers
                    .xssProtection(xss ->
                        xss.headerValue(
                            org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK
                        )
                    )
                    .contentSecurityPolicy(csp ->
                        csp.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; connect-src 'self' wss: ws:;"
                        )
                    )
            )
            .authorizeHttpRequests(authorize -> {
                if (isMockSecurity()) {
                    authorize.anyRequest().permitAll(); // Allow all if mock is enabled
                } else {
                    authorize
                        .requestMatchers("/api/admin/simulation/trigger")
                        .permitAll() // Allow simulation trigger to be public
                        .requestMatchers("/api/admin/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll();
                }
            });

        if (!isMockSecurity()) {
            http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        }

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (isMockSecurity() || issuerUri == null || issuerUri.isEmpty()) {
            return token -> null;
        }
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(
            issuerUri
        ).build();
        OAuth2TokenValidator<
            org.springframework.security.oauth2.jwt.Jwt
        > withIssuer =
            org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithIssuer(
                issuerUri
            );
        OAuth2TokenValidator<
            org.springframework.security.oauth2.jwt.Jwt
        > withAudience = new AudienceValidator(audience);
        OAuth2TokenValidator<
            org.springframework.security.oauth2.jwt.Jwt
        > validator = new DelegatingOAuth2TokenValidator<>(
            withIssuer,
            withAudience
        );
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
