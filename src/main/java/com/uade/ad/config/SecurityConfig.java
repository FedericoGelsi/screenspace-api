package com.uade.ad.config;


import com.uade.ad.security.AuthSuccessHandler;
import com.uade.ad.security.JsonObjectAuthenticationFilter;
import com.uade.ad.security.JwtAuthorizationFilter;
import com.uade.ad.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {
    private final AuthSuccessHandler authSuccessHandler;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final String secret;
    @Autowired
    private AuthenticationManager authenticationManager;

    public SecurityConfig(AuthSuccessHandler authSuccessHandler, JwtUserDetailsService jwtUserDetailsService, @Value("${jwt.secret}") String secret) {
        this.authSuccessHandler = authSuccessHandler;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.secret = secret;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests((auth) -> {
                    try {
                        auth
                            .requestMatchers("/api/auths/**").permitAll()
                            .requestMatchers("/api/user/**").permitAll()
                            //.requestMatchers(HttpMethod.GET,"/api/ejemplo").hasAnyAuthority("ADMIN" , "USER")
                            .anyRequest().permitAll()
                            .and()
                            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                            .and()
                            .formLogin()
                                .permitAll()
                            .and()
                                .logout()
                                .permitAll()
                                .and()
                            .addFilter(authenticationFilter())
                            .addFilter(new JwtAuthorizationFilter(authenticationManager, jwtUserDetailsService, secret))
                            .exceptionHandling()
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .httpBasic(Customizer.withDefaults());
        http.headers().frameOptions().disable();
        return http.build();
    }

    @Bean
    public JsonObjectAuthenticationFilter authenticationFilter() throws Exception {
        JsonObjectAuthenticationFilter filter = new JsonObjectAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(authSuccessHandler);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

}

