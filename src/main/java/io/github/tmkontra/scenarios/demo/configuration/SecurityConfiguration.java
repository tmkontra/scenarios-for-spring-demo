package io.github.tmkontra.scenarios.demo.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    public static final String ADMIN_AUTHORIZATION_HEADER = "topsecret";
    public static final String CUSTOMER_AUTHORIZATION_HEADER = "fake-customer-token";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable the HTML login form page
                .formLogin(form -> form.disable())

                // 2. Disable standard HTTP Basic login dialog (if using JWT/OAuth2)
                .httpBasic(basic -> basic.disable())

                // 3. Turn off CSRF protection (typical for stateless REST APIs)
                .csrf(csrf -> csrf.disable())

                .addFilterBefore(new StaticAuthorizationHeaderAuthenticationFilter(), AnonymousAuthenticationFilter.class)

                // 4. Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 5. Force Spring to return 401 Unauthorized instead of redirecting
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    private static class StaticAuthorizationHeaderAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            if (ADMIN_AUTHORIZATION_HEADER.equals(request.getHeader("Authorization"))) {
                User principal = new User(
                        "superuser",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (CUSTOMER_AUTHORIZATION_HEADER.equals(request.getHeader("Authorization"))) {
                User principal = new User(
                        "customer",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }
    }
}
