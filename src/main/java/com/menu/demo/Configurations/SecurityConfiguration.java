package com.menu.demo.Configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.menu.demo.SecurityJwt.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth

                // ── Public endpoints ──────────────────────────────────────
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/schools").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/students/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/school-requests").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/modules/browse").permitAll()

                // ── Super admin only ──────────────────────────────────────
                .requestMatchers("/api/platform/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/school-requests/pending").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/school-requests/*/approve").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/school-requests/*/reject").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/schools/*/suspend").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/schools/*/reactivate").hasRole("SUPER_ADMIN")

                // ── School admin only ─────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/classrooms").hasRole("SCHOOL_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/subjects").hasRole("SCHOOL_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/teachers").hasRole("SCHOOL_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/modules").hasRole("SCHOOL_ADMIN")
                .requestMatchers("/api/enrollments/requests/**").hasRole("SCHOOL_ADMIN")
                .requestMatchers("/api/invoices/school/**").hasRole("SCHOOL_ADMIN")

                // ── Student only ──────────────────────────────────────────
                .requestMatchers("/api/enrollments/request").hasRole("STUDENT")
                .requestMatchers("/api/enrollments/mine").hasRole("STUDENT")
                .requestMatchers("/api/invoices/mine").hasRole("STUDENT")

                // ── Everything else just needs to be authenticated ────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("POST","GET","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}