package com.firomsa.inventory.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.firomsa.inventory.v1.service.JWTAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTSecurityFilter extends OncePerRequestFilter {

    private JWTAuthService jwtAuthService;
    private UserDetailsService userDetailsService;

    public JWTSecurityFilter(
            JWTAuthService jwtAuthService,
            UserDetailsService userDetailsService) {
        this.jwtAuthService = jwtAuthService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.split(" ")[1];
        }
        if (token != null) {
            String email = jwtAuthService.getSubject(token);
            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(
                        email);
                boolean isValid = jwtAuthService.isValidToken(
                        token,
                        user.getUsername());
                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            user.getPassword(),
                            user.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
