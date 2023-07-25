package zipdabang.server.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.OAuthResult;
import zipdabang.server.web.dto.requestDto.UserRequestDto;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

import java.io.IOException;

@RestController
@Validated
@RequiredArgsConstructor
public class UserRestController {

    //소셜로그인
    @PostMapping("/users/oauth")
    public ResponseDto<OAuthResult.OAuthResultDto> oauth(
            @PathVariable String type,
            @RequestBody UserRequestDto.KakaoSocialDto kakaoRequest,
            @RequestBody UserRequestDto.AppleSocialDto appleRequest) {

        return null;
    }

    //회원 정보 추가입력
    @PostMapping("/users/oauth/info")
    public ResponseDto<UserResponseDto.JoinUserDto> userInfoForSignUp(@RequestBody UserRequestDto.UserInfoDto request) {
        return null;
    }

    //인증번호 요청
    @PostMapping("/users/phone/sms")
    public ResponseDto<Integer> sendSms(@RequestBody UserRequestDto.SmsRequestDto request){
        return null;
    }

    //인증번호 검증
    @PostMapping("/users/phone/auth")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> authPhoneNum(@RequestBody UserRequestDto.PhoneNumAuthDto request){
        return null;
    }
}
