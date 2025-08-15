package az.company.mspayment.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/stripe/webhook", "/stripe/webhook/**").permitAll()
                        .requestMatchers("/pay/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new GatewayAuthHeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        return http.build();
    }

    static class GatewayAuthHeaderFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, jakarta.servlet.ServletException {

            String path = request.getRequestURI();

            if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
                chain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/stripe/webhook") || path.startsWith("/pay/")) {
                chain.doFilter(request, response);
                return;
            }


            String internal = request.getHeader("X-Internal-Gateway");
            if (!"true".equalsIgnoreCase(internal)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String userId = request.getHeader("X-User-ID");
            String username = request.getHeader("X-Username");
            String role = request.getHeader("X-role");

            if (userId != null && role != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        username == null ? "gateway-user" : username,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);
        }
    }

}

