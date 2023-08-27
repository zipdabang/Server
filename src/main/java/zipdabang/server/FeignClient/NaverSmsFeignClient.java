package zipdabang.server.FeignClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsRequestDto;


@FeignClient(name = "feign", url = "https://sens.apigw.ntruss.com/sms/v2/services", configuration = NaverFeignConfiguration.class)
public interface NaverSmsFeignClient {

    @PostMapping(value = "/{serviceId}/messages")
    void sendSms(@PathVariable(value = "serviceId") String serviceId,
                 @RequestHeader HttpHeaders headers,
                 @RequestBody SmsRequestDto request);
}
