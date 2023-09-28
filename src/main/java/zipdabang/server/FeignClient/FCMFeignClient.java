package zipdabang.server.FeignClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import zipdabang.server.FeignClient.Config.FCMFeignConfiguration;
import zipdabang.server.FeignClient.dto.fcm.FCMResponseDto;
import zipdabang.server.firebase.fcm.dto.FcmAOSMessage;

@FeignClient(name = "FCMFeign", url = "https://fcm.googleapis.com", configuration = FCMFeignConfiguration.class)
@Component
public interface FCMFeignClient {


    @PostMapping("/v1/projects/zipdabang-android/messages:send")
    FCMResponseDto getFCMResponse(@RequestHeader("Authorization") String token, @RequestBody String fcmAOSMessage);
}
