package com.logicleaf.invplatform.security;

import com.logicleaf.invplatform.config.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            return chain.filter(exchange); // Invalid token, proceed without authentication
        }

        if (email != null) {
            Mono<UserDetails> userDetailsMono = userDetailsService.loadUserByUsername(email);

            return userDetailsMono.flatMap(userDetails -> {
                if (jwtService.isTokenValid(token, userDetails.getUsername())) { // getUsername() will return the email
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                    Mono.just(new SecurityContextImpl(authentication))
                            ));
                }
                return chain.filter(exchange);
            }).switchIfEmpty(chain.filter(exchange)); // If user not found, continue without auth
        }

        return chain.filter(exchange);
    }
}
