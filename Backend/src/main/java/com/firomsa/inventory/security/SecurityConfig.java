package com.firomsa.inventory.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.firomsa.inventory.config.AllowedOrigins;
import com.firomsa.inventory.model.Roles;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JWTSecurityFilter jwtSecurityFilter;
        private final AllowedOrigins allowedOrigins;
        private final UnAuthorizedUserAuthenticationEntryPoint unAuthorizedUserAuthenticationEntryPoint;

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigins.getOrigins());
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
                configuration.setAllowedHeaders(List.of("Authorization"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers("/api/v1/auth/**", "/docs",
                                                                "/v3/api-docs", "/v3/api-docs/**",
                                                                "/swagger-resources/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**")
                                                .permitAll().requestMatchers("/api/v1/admin/**")
                                                .hasRole(Roles.ADMIN.name())
                                                .requestMatchers("/api/v1/employee/**")
                                                .hasRole(Roles.EMPLOYEE.name()).anyRequest()
                                                .authenticated())
                                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                                                unAuthorizedUserAuthenticationEntryPoint))
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtSecurityFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }
}
