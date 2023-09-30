package zipdabang.server.FeignClient.Config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import zipdabang.server.FeignClient.exception.FeignClientExceptionErrorDecoder;

@RequiredArgsConstructor
public class NaverFeignConfiguration {

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new ColonInterceptor();
    }
    public static class ColonInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            template.uri(template.path().replaceAll("%3A", ":"));
        }
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return  new FeignClientExceptionErrorDecoder();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }




}
