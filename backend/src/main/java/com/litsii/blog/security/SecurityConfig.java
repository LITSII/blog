package com.litsii.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            SecurityContextRepository contextRepository,
                                            CorsConfigurationSource corsSource) throws Exception {
        // 프론트(JS)가 XSRF-TOKEN 쿠키를 읽어 X-XSRF-TOKEN 헤더로 보내는 방식
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // 지연 로딩 비활성 → 매 요청 쿠키 보장

        http
            .cors(c -> c.configurationSource(corsSource))
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(csrfHandler))
            .securityContext(sc -> sc.securityContextRepository(contextRepository))
            .sessionManagement(sm -> sm
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(f -> f.changeSessionId()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().denyAll())
            // 리다이렉트/로그인 폼 없이 401만 반환
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .logout(l -> l.disable())
            .headers(h -> h
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                    .frameOptions(f -> f.deny()));

        return http.build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(BlogProperties props) {
        var admin = props.admin();
        if (admin == null || !StringUtils.hasText(admin.passwordHash())) {
            throw new IllegalStateException("ADMIN_PASSWORD_HASH 환경변수가 설정되지 않았습니다.");
        }
        if (!admin.passwordHash().startsWith("{bcrypt}")) {
            throw new IllegalStateException("ADMIN_PASSWORD_HASH는 {bcrypt} 접두사가 붙은 BCrypt 해시여야 합니다.");
        }
        return new InMemoryUserDetailsManager(User.withUsername(admin.username())
                .password(admin.passwordHash())
                .roles("ADMIN")
                .build());
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService uds, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(BlogProperties props) {
        var source = new UrlBasedCorsConfigurationSource();
        List<String> origins = (props.cors() == null || props.cors().allowedOrigins() == null)
                ? List.of()
                : props.cors().allowedOrigins().stream().filter(StringUtils::hasText).toList();
        if (!origins.isEmpty()) {
            var config = new CorsConfiguration();
            config.setAllowedOrigins(origins);
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
            config.setAllowCredentials(true);
            source.registerCorsConfiguration("/api/**", config);
        }
        return source;
    }
}
