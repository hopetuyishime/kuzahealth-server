package rw.ac.auca.kuzahealth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // http
        //     .csrf(csrf -> csrf.disable()) // Consider enabling CSRF for production
        //     .authorizeHttpRequests(authz -> authz
        //         .requestMatchers("/api/users/**").permitAll() // Allow registration without authentication
        //         .requestMatchers("/api/visits/").permitAll() // Allow registration without authentication
        //         .requestMatchers("/login").permitAll() // Allow access to the login page
        //         .requestMatchers("/public/**").permitAll() // Allow access to other public endpoints
        //         .anyRequest().authenticated() // Authenticate all other requests
        //     )
        //     .formLogin(form -> form
        //         .loginPage("/login")
        //         .permitAll()
        //         // .defaultSuccessUrl("/home", true) // Redirect to home on successful login
        //         // .failureUrl("/login?error=true") // Redirect to login with error
        //     )
        //     .logout(logout -> logout
        //         .permitAll()
        //     );
        http
        .csrf(csrf -> csrf.disable()) // Disable CSRF for testing purposes (enable for production)
        .authorizeHttpRequests(authz -> authz
            .anyRequest().permitAll() // Permit all requests for testing
        );
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }
}

