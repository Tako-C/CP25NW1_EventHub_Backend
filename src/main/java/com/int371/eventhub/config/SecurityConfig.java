package com.int371.eventhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> {
                                }) // <<< ต้องมีตรงนี้
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                                                .requestMatchers("/surveys/**").permitAll()
                                                .requestMatchers("/error").permitAll()

                                                .requestMatchers(HttpMethod.POST, "/events/*/register/otp/request")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/events/*/register/otp/verify")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/events/**", "/event/**","/api/events/**")
                                                .permitAll()

                                                // .requestMatchers(HttpMethod.GET, "/checkins").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/surveys/submit/**").hasAnyRole("GENERAL_USER","ADMIN", "SURVEY_GUEST")

                                                .requestMatchers("/users/**").hasAnyRole("GENERAL_USER","ADMIN")

                                                .requestMatchers("/events/*/surveys/answers**").hasAnyRole("GENERAL_USER","ADMIN", "SURVEY_GUEST")

                                                .requestMatchers(HttpMethod.POST, "/events/*/register").hasAnyRole("GENERAL_USER","ADMIN")

                                                .requestMatchers("/upload/qr/**").hasAnyRole("GENERAL_USER","ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/upload/**").permitAll()
                                                // .requestMatchers(HttpMethod.POST, "/events/*/register").authenticated()
                                                // .requestMatchers("/users/**").authenticated()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsFilter corsFilter() {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOriginPattern("*"); // รองรับ http + https
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return new CorsFilter(source);
        }

}