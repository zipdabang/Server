package zipdabang.server.FeignClient;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import zipdabang.server.FeignClient.exception.FeignClientExceptionErrorDecoder;

public class KakaoFeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return template -> template.header("Content-Type", "application/x-www-form-urlencoded");
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
