package com.api.e_commerce.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        boolean checkoutRequest = "POST".equalsIgnoreCase(request.getMethod())
                && request.getServletPath().matches("/customers/[0-9a-fA-F-]{36}/cart/checkout");
        if (checkoutRequest
                && (authorization == null || !authorization.startsWith("Bearer "))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bearer token is required");
            return;
        }
        if (authorization != null
                && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(authorization.substring(7), request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            String email = jwtService.validateAndGetSubject(token);
            var user = userDetailsService.loadUserByUsername(email);
            boolean customer = user.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("CUSTOMER_CHECKOUT"));
            if (!customer || !user.isEnabled()) {
                return;
            }

            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    user, null, user.getAuthorities());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            var securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
        } catch (RuntimeException exception) {
            LOGGER.debug("JWT authentication failed: {}", exception.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
