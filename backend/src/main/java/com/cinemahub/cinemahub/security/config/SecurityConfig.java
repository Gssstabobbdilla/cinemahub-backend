package com.cinemahub.cinemahub.security.config;

import com.cinemahub.cinemahub.security.jwt.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Debes iniciar sesión\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"No tienes permisos para esta acción\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // Catálogo público de solo lectura
                .requestMatchers(HttpMethod.GET,
                        "/api/movies/**", "/api/classifications/**", "/api/genres/**",
                        "/api/showtimes/**", "/api/cinemas/**", "/api/rooms/**", "/api/seats/**",
                        "/api/products/**", "/api/product-categories/**", "/api/promotions/**",
                        "/api/coupons/**"
                ).permitAll()

                // Escritura sobre catálogo: solo administradores
                .requestMatchers(HttpMethod.POST,
                        "/api/movies/**", "/api/classifications/**", "/api/genres/**",
                        "/api/cinemas/**", "/api/rooms/**", "/api/seats/**", "/api/showtimes",
                        "/api/products/**", "/api/product-categories/**",
                        "/api/promotions/**", "/api/promotions/*/coupons"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/movies/**", "/api/classifications/**", "/api/genres/**",
                        "/api/cinemas/**", "/api/products/**", "/api/product-categories/**"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,
                        "/api/movies/*/status", "/api/promotions/*/status"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/genres/**", "/api/classifications/**", "/api/cinemas/**",
                        "/api/rooms/**", "/api/seats/**", "/api/showtimes/**", "/api/product-categories/**"
                ).hasRole("ADMIN")

                // Gestión de usuarios/roles/permisos: solo administradores
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasRole("ADMIN")
                .requestMatchers("/api/users/*/roles/**").hasRole("ADMIN")
                .requestMatchers("/api/roles/**", "/api/permissions/**").hasRole("ADMIN")

                // Todo lo demás (reservas, órdenes, pagos, membresías, notificaciones,
                // perfil propio) requiere solo estar autenticado.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}