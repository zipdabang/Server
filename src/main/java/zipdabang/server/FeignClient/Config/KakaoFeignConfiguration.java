package zipdabang.server.FeignClient.Config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import zipdabang.server.FeignClient.exception.FeignClientExceptionErrorDecoder;

public class KakaoFeignConfiguration {

//    @Bean
//    public RequestInterceptor requestInterceptor(){ return null;
//    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return  new FeignClientExceptionErrorDecoder();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
