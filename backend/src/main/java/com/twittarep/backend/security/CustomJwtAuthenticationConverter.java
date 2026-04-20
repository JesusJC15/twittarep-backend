package com.twittarep.backend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        return new JwtAuthenticationToken(source, extractAuthorities(source), source.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scopeString) {
            for (String scope : scopeString.split(" ")) {
                if (!scope.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }
        }

        Object permissions = jwt.getClaims().get("permissions");
        if (permissions instanceof List<?> permissionList) {
            permissionList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(value -> new SimpleGrantedAuthority("SCOPE_" + value))
                .forEach(authorities::add);
        }

        Object customClaims = jwt.getClaims().get("https://twittarep.example.com/roles");
        if (customClaims instanceof List<?> roles) {
            roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
        }
        return authorities;
    }
}
