package com.medbook.config;

import com.medbook.entity.User;
import com.medbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        System.out.println("Attempting to log in user: " + email);
        System.out.println("Stored encoded password: " + user.getPassword());
        // Temporarily log the raw password for debugging. REMOVE THIS IN PRODUCTION.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            System.out.println("Attempted raw password: " + authentication.getCredentials());
        }

        Collection<? extends GrantedAuthority> authorities = mapRoleToAuthorities(user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    private List<GrantedAuthority> mapRoleToAuthorities(User.Role role) {
        // Prefix with ROLE_ to align with hasRole checks
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}







