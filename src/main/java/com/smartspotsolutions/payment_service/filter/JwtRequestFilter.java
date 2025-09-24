package com.smartspotsolutions.payment_service.filter;

import com.smartspotsolutions.payment_service.security.AppUserDetailsService;
import com.smartspotsolutions.payment_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_URL_PATTERNS = List.of(
            "/auth/**",
            "/payments/daraja/callback",
            "/users/register",
            // Allow SpringDoc OpenAPI/Swagger UI resources
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/public/**",
            "/oauth2/authorization/google"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath(); // Use getServletPath() for matching configured patterns

        // Check if the current path matches any of the public URL patterns
        boolean isPublicUrl = PUBLIC_URL_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublicUrl) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Extract token
        String jwt = jwtUtil.getJwtTokenFromHttpRequest(request);
        String email = null;

        // 2. Validate the token and set security context
        // Only proceed if a JWT is present and there's no existing authentication in the context
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            email = jwtUtil.extractEmail(jwt);

            if (email != null) { // Only proceed if email can be extracted
                UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);
                // No need for a duplicate call to loadUserByUsername
                if (jwtUtil.isValidToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}