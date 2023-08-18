package zipdabang.server.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import zipdabang.server.auth.handler.JwtAccessDeniedHandler;
import zipdabang.server.auth.handler.JwtAuthenticationEntryPoint;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.redis.service.RedisService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final UrlBasedCorsConfigurationSource corsConfigurationSource;

    private final RedisService redisService;

    private final TokenProvider tokenProvider;
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring()
                .antMatchers(
                        "/favicon.ico",
                        "/health",
                        "/",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/docs/**","/members/oauth", "/members/oauth/info","/members/new-token","/members/terms"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http.
                cors()
                .configurationSource(corsConfigurationSource)
                .and()
                .csrf().disable()

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/**", "/swagger-ui.html").permitAll()


                .and()
                .apply(new JwtSecurityConfig(tokenProvider, redisService))
                .and().build();
    }
}
