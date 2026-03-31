package lk.sliit.customer_care.config;

import lk.sliit.customer_care.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/auth/register", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/faq", "/faq/**").permitAll() // Allow public access to FAQ
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/agent/**").hasRole("AGENT")
                        .requestMatchers("/user/**", "/tickets/**", "/feedback/**", "/profile").hasAnyRole("USER", "AGENT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthSuccessHandler()) // Custom redirect after successful login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // Enable CSRF for forms, disable only for API endpoints

        return http.build();
    }

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // User details service to load users from DB
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Hardcode the admin user
            if (username.equals("admin")) {
                return org.springframework.security.core.userdetails.User
                        .withUsername("admin")
                        .password(passwordEncoder().encode("admin1234")) // Preconfigured password for admin
                        .roles("ADMIN")
                        .build();
            }
            return userRepository.findByUsername(username)
                    .map(user -> org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword()) // already encoded
                            .roles(user.getRole().replace("ROLE_", "")) // Spring adds ROLE_ prefix
                            .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        };
    }

    // Custom redirect handler after successful login
    @Bean
    public AuthenticationSuccessHandler customAuthSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {
                String role = authentication.getAuthorities().iterator().next().getAuthority();

                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                } else if (role.equals("ROLE_AGENT")) {
                    response.sendRedirect("/agent/dashboard");
                } else {
                    response.sendRedirect("/user/dashboard");
                }
            }
        };
    }
}
