package com.api.e_commerce.infrastructure.security;

import com.api.e_commerce.domain.security.AdministratorPermission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/customers", "/customers/authentication")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories", "/categories/*/products")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/administrators")
                        .hasAuthority(AdministratorPermission.ADMINISTRATOR_MANAGE.name())
                        .requestMatchers(HttpMethod.GET, "/administrators")
                        .hasAuthority(AdministratorPermission.ADMINISTRATOR_MANAGE.name())
                        .requestMatchers(HttpMethod.POST, "/categories")
                        .hasAuthority(AdministratorPermission.CATEGORY_MANAGE.name())
                        .requestMatchers(HttpMethod.GET,
                                "/customers",
                                "/customers/email/*",
                                "/customers/email/*/purchase-analysis")
                        .hasAuthority(AdministratorPermission.CUSTOMER_READ.name())
                        .requestMatchers(HttpMethod.POST, "/products")
                        .hasAuthority(AdministratorPermission.PRODUCT_CREATE.name())
                        .requestMatchers(HttpMethod.POST, "/products/import/fake-store")
                        .hasAuthority(AdministratorPermission.PRODUCT_IMPORT.name())
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/administrators/*/financial-summary")
                        .hasAuthority(AdministratorPermission.FINANCIAL_READ.name())
                        .requestMatchers("/customers/*/cart", "/customers/*/cart/**")
                        .hasAuthority("CUSTOMER_CHECKOUT")
                        .anyRequest()
                        .denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins:http://localhost:8080}")
            List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Idempotency-Key"
        ));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
