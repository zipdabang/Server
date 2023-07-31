package zipdabang.server.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.FeignClient.dto.OAuthInfoDto;
import zipdabang.server.FeignClient.service.KakaoOauthService;
import zipdabang.server.base.Code;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.service.MemberService;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.OAuthResult;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "유저 관련 API", description = "로그인, 회원가입, 마이 페이지에서 필요한 API모음")
public class MemberRestController {

    private final MemberService memberService;

    private final KakaoOauthService kakaoOauthService;

    @PostMapping("/members/logout")
    public ResponseDto<MemberResponseDto.memberStatusDto> logout(@RequestBody MemberRequestDto.logoutMember request) {
        return null;
    }

    @PatchMapping("/members/quit")
    public ResponseDto<MemberResponseDto.memberStatusDto> quit(@RequestBody MemberRequestDto.quitMember request) {
        return null;
    }

    @PatchMapping("/members/restore")
    public ResponseDto<MemberResponseDto.memberStatusDto> restore(@RequestBody MemberRequestDto.restoreMember request) {
        return null;
    }

    //소셜로그인
    @PostMapping("/members/oauth/kakao")
    public ResponseDto<MemberResponseDto.SocialLoginDto> oauthKakao(
            @RequestBody MemberRequestDto.OAuthRequestDto oAuthRequestDto) {
        OAuthInfoDto kakaoUserInfo = kakaoOauthService.getKakaoUserInfo(oAuthRequestDto.getToken());
        OAuthResult.OAuthResultDto oAuthResultDto = memberService.kakaoSocialLogin(kakaoUserInfo.getEmail(), kakaoUserInfo.getProfileUrl());
        MemberResponseDto.SocialLoginDto socialLoginDto = MemberConverter.toSocialLoginDto(oAuthResultDto.getJwt());
        return oAuthResultDto.getIsLogin() ? ResponseDto.of(Code.OAUTH_LOGIN,socialLoginDto) : ResponseDto.of(Code.OAUTH_JOIN,socialLoginDto);
    }

    @PostMapping("/members/oauth/google")
    public ResponseDto<OAuthResult.OAuthResultDto> oauthGoogle(
            @RequestBody MemberRequestDto.OAuthRequestDto oAuthRequestDto) {
        return null;
    }

    //회원 정보 추가입력
    @PostMapping("/members/oauth/info")
    public ResponseDto<MemberResponseDto.JoinMemberDto> memberInfoForSignUp(@RequestBody MemberRequestDto.MemberInfoDto request) {
        return null;
    }

    //인증번호 요청
    @PostMapping("/members/phone/sms")
    public ResponseDto<Integer> sendSms(@RequestBody MemberRequestDto.SmsRequestDto request) {
        return null;
    }

    //인증번호 검증
    @PostMapping("/members/phone/auth")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> authPhoneNum(@RequestBody MemberRequestDto.PhoneNumAuthDto request) {return null;}


    //프로필 수정
    @PatchMapping(value = "/members",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ResponseDto<MemberResponseDto.memberStatusDto> updateProfile (@ModelAttribute MemberRequestDto.memberProfileDto request )
    {
        return null;
    }

    //프로필 조회
    @GetMapping("/members/{memberId}")
    public ResponseDto<MemberResponseDto.MemberProfileDto> showProfile (@PathVariable("memberId") Long memberId){
        return null;
    }

    //내 프로필 조회
    @GetMapping("/members")
    public ResponseDto<MemberResponseDto.MemberProfileDto> showMyProfile (){
        return null;
    }

    //닉네임 중복검사
    @GetMapping("/members/exist-nickname")
    public ResponseDto<String> checkExistNickname (@RequestParam String nickname){
        return null;
    }
}
