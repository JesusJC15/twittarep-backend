package com.twittarep.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.twittarep.backend.dto.ErrorResponse;
import com.twittarep.backend.security.AudienceValidator;
import com.twittarep.backend.security.CustomJwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    private static final ObjectMapper SECURITY_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            CustomJwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/stream").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts").hasAuthority("SCOPE_write:posts")
                .requestMatchers(HttpMethod.GET, "/api/me").hasAuthority("SCOPE_read:profile")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, authException) ->
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication failed", request.getRequestURI());
    }

    @Bean
    AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
            writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Access denied", request.getRequestURI());
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder jwtDecoder(AppProperties appProperties,
                          org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties properties) {
        String issuerUri = properties.getJwt().getIssuerUri();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        var issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        var audienceValidator = new AudienceValidator(appProperties.getSecurity().getAudience());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(issuerValidator, audienceValidator));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties appProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appProperties.getCors().getAllowedOrigins().isEmpty()
            ? List.of("http://localhost:5173")
            : appProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    int status,
                                    String error,
                                    String message,
                                    String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ErrorResponse body = new ErrorResponse(Instant.now(), status, error, message, path);
        SECURITY_OBJECT_MAPPER.writeValue(response.getOutputStream(), body);
    }
}
