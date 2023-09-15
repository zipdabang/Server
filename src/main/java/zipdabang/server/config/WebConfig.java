package zipdabang.server.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zipdabang.server.auth.handler.annotation.Resolver.AuthMemberArgumentResolver;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthMemberArgumentResolver authMemberArgumentResolver;
    private final CorsProperties corsProperties;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolverList) {
        resolverList.add(authMemberArgumentResolver);
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 출처를 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList("https://www.zipdabang.shop"));
        configuration.setAllowedOrigins(Arrays.asList("https://api.zipdabang.shop"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        corsConfigSource.registerCorsConfiguration("/**", configuration);
        return corsConfigSource;
    }
}
