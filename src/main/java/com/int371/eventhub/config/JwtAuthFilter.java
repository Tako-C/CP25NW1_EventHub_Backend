package com.int371.eventhub.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.int371.eventhub.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsService userDetailsService,
                         HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth") 
        || path.startsWith("/api/auth")
        || path.startsWith("/surveys")
        || path.equals("/error");
    }

    // @Override
    // @SuppressWarnings("UseSpecificCatch")
    // protected void doFilterInternal(
    //         @NonNull HttpServletRequest request,
    //         @NonNull HttpServletResponse response,
    //         @NonNull FilterChain filterChain
    // ) throws ServletException, IOException {


    // final String path = request.getServletPath();

    //     if ((path.startsWith("/auth") 
    //         || path.startsWith("/api/auth"))
    //         || path.startsWith("/surveys")) 
    //         {
    //         filterChain.doFilter(request, response);
    //         return;
    //     }

    //     final String authHeader = request.getHeader("Authorization");

    //     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    //         filterChain.doFilter(request, response);
    //         return;
    //     }

    //     try {
    //         final String jwt = authHeader.substring(7);
    //         final String userEmail = jwtService.extractUsername(jwt);

    //         if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    //             UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
    //             if (jwtService.isTokenValid(jwt, userDetails)) {
    //                 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    //                         userDetails, null, userDetails.getAuthorities()
    //                 );
    //                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    //                 SecurityContextHolder.getContext().setAuthentication(authToken);
    //             }
    //         }
    //         filterChain.doFilter(request, response);
    //     } catch (Exception e) {
    //         handlerExceptionResolver.resolveException(request, response, null, e);
    //     }
    // }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();

        // ใช้ shouldNotFilter ที่คุณเขียนไว้แล้ว (ซึ่งรวม /error ไว้แล้ว)
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // ถ้าไม่มี Header หรือไม่ขึ้นต้นด้วย Bearer ให้ส่งต่อไปยัง SecurityFilterChain 
        // เพื่อให้มันเช็ค permitAll() หรือดัก 401 ตามปกติ
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    String tokenRole = jwtService.extractClaim(jwt, claims -> claims.get("tokenRole", String.class));
                    
                    java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;
                    
                    if (tokenRole != null) {
                        authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + tokenRole));
                    } else {
                        authorities = userDetails.getAuthorities();
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // หากเกิด Error (เช่น Token หมดอายุ) ให้เช็คว่าถ้าเป็น Public Path ให้ปล่อยผ่าน
            if (path.startsWith("/events/") || path.startsWith("/upload/")) {
                filterChain.doFilter(request, response);
            } else {
                handlerExceptionResolver.resolveException(request, response, null, e);
            }
        }
    }
}