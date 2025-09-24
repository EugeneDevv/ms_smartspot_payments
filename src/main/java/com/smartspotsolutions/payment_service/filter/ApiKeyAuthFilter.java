package com.smartspotsolutions.payment_service.filter;

import com.smartspotsolutions.payment_service.entity.RegisteredServiceEntity;
import com.smartspotsolutions.payment_service.service.ServiceRegistry;
import com.smartspotsolutions.payment_service.util.ApiKeyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ServiceRegistry serviceRegistry;
    private final ApiKeyManager apiKeyManager;

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_SECRET_HEADER = "X-API-SECRET";


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        String apiKey = request.getHeader(API_KEY_HEADER);
        String apiSecret = request.getHeader(API_SECRET_HEADER);

        if (apiKey == null || apiSecret == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var service = serviceRegistry.findByApiKey(apiKey);
        if (service.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }

        RegisteredServiceEntity serviceEntity = service.get();

        if (!apiKeyManager.matches(apiSecret, serviceEntity.getApiSecretHash())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Secret");
            return;
        }

        // authenticated - set service info in SecurityContext
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                serviceEntity.getServiceId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}


