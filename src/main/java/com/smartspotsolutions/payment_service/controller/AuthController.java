package com.smartspotsolutions.payment_service.controller;

import com.smartspotsolutions.payment_service.exception.ApiErrorResponse;
import com.smartspotsolutions.payment_service.io.request.AuthRequest;
import com.smartspotsolutions.payment_service.io.response.AuthResponse;
import com.smartspotsolutions.payment_service.io.response.UserResponse;
import com.smartspotsolutions.payment_service.security.AppUserDetailsService;
import com.smartspotsolutions.payment_service.service.UserService;
import com.smartspotsolutions.payment_service.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final HttpServletRequest request;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.cookie.max-age-days:1}")
    private int jwtCookieMaxAgeDays;

    @Value("${jwt.cookie.secure:true}")
    private boolean jwtCookieSecure;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // 1. Authenticate user credentials
            authenticate(authRequest.getEmail(), authRequest.getPassword());

            // 2. Load UserDetails for JWT generation
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(authRequest.getEmail());

            // 3. Get detailed UserResponse (including verification status)
            Optional<UserResponse> userProfileOptional = userService.getUserByEmail(authRequest.getEmail());
            UserResponse userProfile = userProfileOptional.orElseThrow(
                    () -> new UsernameNotFoundException("User not found after successful authentication.")
            );

            // 4. Generate JWT Token
            List<String> roles = userDetails.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            final String jwtToken = jwtUtil.generateToken(userDetails, roles);

            // 5. Create JWT Cookie
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true) // Prevents client-side JS access
                    .path("/")     // Available across the whole application
                    .maxAge(Duration.ofDays(jwtCookieMaxAgeDays)) // Configurable max age
                    .sameSite("Strict") // Strongest same-site policy for CSRF protection
                    .secure(jwtCookieSecure) // Set to true in production with HTTPS
                    .build();

            // 6. Return AuthResponse with user profile
            AuthResponse authResponse = new AuthResponse(jwtToken, userProfile);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(authResponse);
        } catch (BadCredentialsException exception) {
            log.warn("Login failed for email {}: Invalid credentials.", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                            .message("Invalid email or password.")
                            .path(request.getRequestURI())
                            .details(Collections.singletonMap("code", "BAD_CREDENTIALS")) // Use map for details
                            .build());
        } catch (DisabledException exception) {
            log.warn("Login failed for email {}: Account disabled.", authRequest.getEmail());
            Optional<UserResponse> user = userService.getUserByEmail(authRequest.getEmail());

            String message;
            String errorCode;
            HttpStatus status = HttpStatus.UNAUTHORIZED;

            if (user.isPresent()) {
                if (!user.get().getEmailVerified()) {
                    message = "Account not verified. Please check your email or phone for verification instructions.";
                    errorCode = "ACCOUNT_NOT_VERIFIED";
                } else {
                    message = "Your account is disabled. Please contact support.";
                    errorCode = "ACCOUNT_DISABLED";
                }
            } else {
                // Fallback in case user details somehow disappear after authentication manager but before userService lookup
                message = "Your account is disabled.";
                errorCode = "ACCOUNT_DISABLED_UNKNOWN";
            }

            return ResponseEntity.status(status)
                    .body(ApiErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(status.value())
                            .error(status.getReasonPhrase())
                            .message(message)
                            .path(request.getRequestURI())
                            .details(Collections.singletonMap("code", errorCode))
                            .build());
        }  catch (UsernameNotFoundException ex) {
            // This case should be caught by BadCredentialsException if Spring Security is configured correctly,
            // but included for robustness if AppUserDetailsService throws it directly after authentication.
            log.warn("Login failed for email {}: User not found (should be caught by BadCredentials).", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                            .message("Invalid email or password.") // Keep message generic for security
                            .path(request.getRequestURI())
                            .details(Collections.singletonMap("code", "USER_NOT_FOUND"))
                            .build());
        } catch (AuthenticationException ex) {
            log.error("Unhandled authentication error for email {}: {}", authRequest.getEmail(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                            .message("Authentication failed due to an unexpected error. Please try again.")
                            .path(request.getRequestURI())
                            .details(Collections.singletonMap("code", "AUTHENTICATION_ERROR"))
                            .build());
        }
    }

    @GetMapping("/isAuthenticated")
    public ResponseEntity<Boolean> isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // An authenticated user will have an Authentication object that is not null
        // AND is not an instance of AnonymousAuthenticationToken.
        // Also, it should be marked as authenticated.
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() && // Should be true for real users, false for anonymous
                !(authentication instanceof AnonymousAuthenticationToken);

        return ResponseEntity.ok(isAuthenticated);
    }

    private void authenticate(String email, String password) throws AuthenticationException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
