package com.medbook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import com.medbook.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Removed @Autowired for CustomUserDetailsService

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.GET, "/", "/home", "/about", "/contact", "/partners", "/services", "/search", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/login", "/register", "/register/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/forgot-password", "/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/h2-console/**").permitAll()
                .requestMatchers("/patient/**").hasRole("PATIENT")
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // .logout(logout -> logout
            //     .logoutUrl("/logout")
            //     .logoutSuccessUrl("/") // Redirect to root after logout
            //     .invalidateHttpSession(true)
            //     .deleteCookies("JSESSIONID")
            //     .permitAll()
            // )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/login", "/forgot-password", "/reset-password")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService, PasswordEncoder passwordEncoder) {
        return new CustomUserDetailsService(userService, passwordEncoder);
    }
}