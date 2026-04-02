package com.lexdata.juridique.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/test/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/juridique/textes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/juridique/textes").hasAnyRole("AGENT_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/juridique/textes/**").hasAnyRole("AGENT_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/juridique/textes/**").hasAnyRole("AGENT_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/juridique/textes/**").hasAnyRole("AGENT_ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/juridique/favorites/**").authenticated()
                        .requestMatchers("/api/juridique/**").authenticated()
                        .anyRequest().authenticated());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     // Autorise toutes les origines (localhost, Nginx, etc.)
    //     configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    //     // Autorise les méthodes envoyées par Axios
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    //     // Autorise tous les headers (notamment Authorization et Content-Type)
    //     configuration.setAllowedHeaders(Arrays.asList("*"));
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }
}
