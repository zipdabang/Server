package zipdabang.server.sms.service.serviceImpl;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import zipdabang.server.FeignClient.NaverSmsFeignClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.redis.domain.PhoneAuth;
import zipdabang.server.redis.repository.PhoneAuthRepository;
import zipdabang.server.sms.dto.MessageDto;
import zipdabang.server.sms.dto.SmsRequestDto;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.sms.service.SmsService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    private final PhoneAuthRepository phoneAuthRepository;
    private final NaverSmsFeignClient naverSmsFeignClient;

    @Value("${naver-sms.accessKey}")
    private String accessKey;

    @Value("${naver-sms.secretKey}")
    private String secretKey;

    @Value("${naver-sms.serviceId}")
    private String serviceId;

    @Value("${naver-sms.senderPhone}")
    private String phone;

    @Override
    public String makeSignature(Long time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/"+ this.serviceId+"/messages";
        String timestamp = time.toString();
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);
        return encodeBase64String;
    }

    @Override
    @Transactional
    public SmsResponseDto.AuthNumResultDto authNumber(Integer authNum, String phoneNum) {

        Optional<PhoneAuth> phoneAuth = phoneAuthRepository.findById(phoneNum);
        if (phoneAuth.isEmpty()) {
            return SmsResponseDto.AuthNumResultDto.builder()
                    .responseCommonStatus(CommonStatus.PHONE_AUTH_NOT_FOUND)
                    .build();
        }
        CommonStatus commonStatus = CommonStatus.OK;
        if (!phoneAuth.get().getAuthNum().equals(authNum))
            commonStatus = CommonStatus.PHONE_AUTH_ERROR;

        else{
            LocalDateTime nowTime = LocalDateTime.now();

            long timeCheck = ChronoUnit.MINUTES.between(phoneAuth.get().getAuthNumTime(), nowTime);
            if (timeCheck >= 5)
                commonStatus = CommonStatus.PHONE_AUTH_TIMEOUT;

        }
        if(commonStatus.equals(CommonStatus.OK))
            phoneAuthRepository.deleteById(phoneAuth.get().getPhoneNum());

        return SmsResponseDto.AuthNumResultDto.builder()
                .responseCommonStatus(commonStatus)
                .build();
    }

    @Override
    @Transactional
    public SmsResponseDto.AuthNumResultDto sendSms(String targetNumber) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        Long time = System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time));

        List<MessageDto> messages = new ArrayList<>();
        String randomNumber = getRandomNumber();
        StringBuilder sb = new StringBuilder();
        sb.append("[집다방] 인증번호 : ["+randomNumber+"]를 입력해주세요");
        String content = String.valueOf(sb);
        messages.add(MessageDto.builder()
                .to(targetNumber)
                .content(content)
                .build());

        SmsRequestDto request = SmsRequestDto.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(phone)
                .content(content)
                .messages(messages)
                .build();


        naverSmsFeignClient.sendSms(serviceId, headers,request);

        PhoneAuth phoneAuth = PhoneAuth.builder()
                .phoneNum(targetNumber)
                .authNumTime(LocalDateTime.now())
                .authNum(Integer.valueOf(randomNumber))
                .build();
        phoneAuthRepository.save(phoneAuth);


        return SmsResponseDto.AuthNumResultDto.builder()
                .responseCommonStatus(CommonStatus.OK)
                .build();
    }

    @Override
    public String getRandomNumber() {
        Random random = new Random();
        int rand= random.nextInt(900000) + 100000;
        return String.valueOf(rand);
    }
}
