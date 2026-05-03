package com.vibefolio.security;

import com.vibefolio.config.VibefolioProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the {@code X-API-Key} header on every request.
 * Skips public paths (actuator health, Swagger UI, OpenAPI docs).
 * Returns HTTP 401 if the key is absent or does not match the configured value.
 */
@Component
class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final VibefolioProperties properties;

    ApiKeyAuthFilter(VibefolioProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);
        String expectedKey = properties.security().apiKey();

        if (providedKey == null || !providedKey.equals(expectedKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":401,"title":"Unauthorized","detail":"Missing or invalid X-API-Key header"}
                    """);
            return;
        }

        // Mark request as authenticated so Spring Security permits it
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("api-client", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
