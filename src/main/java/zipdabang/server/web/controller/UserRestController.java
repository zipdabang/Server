package zipdabang.server.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.web.dto.requestDto.UserRequestDto;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.OAuthResult;


import java.io.IOException;

@RestController
@Validated
@RequiredArgsConstructor
public class UserRestController {

    @PostMapping("/users/logout")
    public ResponseDto<UserResponseDto.userStatusDto> logout(@RequestBody UserRequestDto.logoutUser request) {
        return null;
    }

    @PatchMapping("/users/quit")
    public ResponseDto<UserResponseDto.userStatusDto> quit(@RequestBody UserRequestDto.quitUser request) {
        return null;
    }

    @PatchMapping("/users/restore")
    public ResponseDto<UserResponseDto.userStatusDto> restore(@RequestBody UserRequestDto.restoreUser request) {
        return null;
    }

    //소셜로그인
    @PostMapping("/users/oauth/kakao")
    public ResponseDto<OAuthResult.OAuthResultDto> oauthKakao(
            @RequestBody UserRequestDto.OAuthRequestDto oAuthRequestDto) {
        return null;
    }

    @PostMapping("/users/oauth/google")
    public ResponseDto<OAuthResult.OAuthResultDto> oauthGoogle(
            @RequestBody UserRequestDto.OAuthRequestDto oAuthRequestDto) {
        return null;
    }

    //회원 정보 추가입력
    @PostMapping("/users/oauth/info")
    public ResponseDto<UserResponseDto.JoinUserDto> userInfoForSignUp(@RequestBody UserRequestDto.UserInfoDto request) {
        return null;
    }

    //인증번호 요청
    @PostMapping("/users/phone/sms")
    public ResponseDto<Integer> sendSms(@RequestBody UserRequestDto.SmsRequestDto request) {
        return null;
    }

    //인증번호 검증
    @PostMapping("/users/phone/auth")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> authPhoneNum(@RequestBody UserRequestDto.PhoneNumAuthDto request) {return null;}


    //프로필 수정
    @PatchMapping(value = "/users",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ResponseDto<UserResponseDto.userStatusDto> updateProfile (@ModelAttribute UserRequestDto.userProfileDto request )
    {
        return null;
    }

    //프로필 조회
    @GetMapping("/users/{userId}")
    public ResponseDto<UserResponseDto.UserProfileDto> showProfile (@PathVariable("userId") Long userId){
        return null;
    }

    //내 프로필 조회
    @GetMapping("/users")
    public ResponseDto<UserResponseDto.UserProfileDto> showMyProfile (){
        return null;
    }

    //닉네임 중복검사
    @GetMapping("/users/exist-nickname")
    public ResponseDto<String> checkExistNickname (@RequestParam String nickname){
        return null;
    }
}
