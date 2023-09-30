package zipdabang.server.firebase.fcm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.FeignClient.FCMFeignClient;
import zipdabang.server.FeignClient.dto.fcm.FCMResponseDto;
import zipdabang.server.firebase.fcm.dto.FcmAOSMessage;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FirebaseServiceImpl implements FirebaseService{

    private String fcmUrl = "https://fcm.googleapis.com/v1/projects/zipdabang-android/messages:send";

    private final ObjectMapper objectMapper;

    private final FCMFeignClient fcmFeignClient;

    Logger logger = LoggerFactory.getLogger(FirebaseServiceImpl.class);

    @Override
    @Transactional
    public void sendMessageTo(String targetToken, String title, String body, String targetView, String targetPK, String targetNotification) throws IOException {
        String aosMessage = makeAOSMessage(targetToken, title, body, targetView, targetPK, targetNotification);

        FCMResponseDto fcmResponse = fcmFeignClient.getFCMResponse("Bearer " + getAccessToken(),aosMessage);
        logger.info("성공? : {}",fcmResponse);
        logger.info("보낸 메세지 : {}",aosMessage);
    }

    private String makeAOSMessage(String targeToken, String title, String body, String targetView, String targetPK,String targetNotification) throws JsonParseException, JsonProcessingException {
        FcmAOSMessage fcmMessage = FcmAOSMessage.builder()
                .message(
                        FcmAOSMessage.Message.builder()
                                .token(targeToken).
                                data(FcmAOSMessage.Data.builder()
                                        .title(title)
                                        .body(body)
                                        .targetView(targetView)
                                        .targetNotification(targetNotification)
                                        .targetPK(targetPK).build()
                                ).
                                build()
                )
                .validateOnly(false).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException{
        String fireBaseConfigPath = "firebase/zipdabang-firebase-key.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(fireBaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
