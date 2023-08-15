package zipdabang.server.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import zipdabang.server.auth.filter.JwtRequestFilter;
import zipdabang.server.auth.handler.JwtAuthenticationExceptionHandler;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.redis.service.RedisService;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private final TokenProvider tokenProvider;

    private final RedisService redisService;

    @Override
    public void configure(HttpSecurity http)throws Exception{
        JwtRequestFilter jwtFilter = new JwtRequestFilter(tokenProvider, redisService);
        JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler = new JwtAuthenticationExceptionHandler();
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationExceptionHandler, JwtRequestFilter.class);
    }
}
