package com.vibefolio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP-layer rate limit filter stub.
 * Per-email and per-IP rate limiting is enforced at the service layer via
 * {@link com.vibefolio.service.RateLimitService} using Bucket4j.
 * This filter is a placeholder for future IP-level throttling at the edge.
 */
@Component
class RateLimitFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Stub: no-op, delegates to service-layer rate limiting
        filterChain.doFilter(request, response);
    }
}
