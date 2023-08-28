package zipdabang.server.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import zipdabang.server.sms.dto.SmsResponseDto;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface SmsService {
    public String makeSignature(Long time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException;
    public SmsResponseDto.AuthNumResultDto authNumber(Integer authNum, String phoneNum);
    public SmsResponseDto.AuthNumResultDto sendSms(String targetNumber) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException;
    public String getRandomNumber();
}
