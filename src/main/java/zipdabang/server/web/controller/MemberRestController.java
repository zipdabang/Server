package zipdabang.server.web.controller;

import feign.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.FeignClient.dto.OAuthInfoDto;
import zipdabang.server.FeignClient.service.KakaoOauthService;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.base.Code;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.MemberService;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.OAuthResult;

import java.util.List;
import java.util.Optional;

@RestController
@Validated
@Slf4j
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

    @GetMapping("/members/category")
    public ResponseDto<List<Category>> getCategoryList() {
        List<Category> categoryList = memberService.getCategoryList();

        log.info("음료 카테고리 리스트: {}", categoryList);
        return ResponseDto.of(categoryList);
    }

    //회원 정보 추가입력
    @PostMapping("/members/oauth/info")
    public ResponseDto<MemberResponseDto.SocialInfoDto> memberInfoForSignUp(@RequestBody MemberRequestDto.MemberInfoDto request, @AuthMember Member member) {
        log.info("body로 넘겨온 사용자 정보: {}", request.toString());
        Member joinMember = memberService.joinInfoComplete(request, member);
        log.info("로그인 된 사용자 정보: {}", member.toString());
        return ResponseDto.of(MemberConverter.toSocialInfoDto(joinMember));
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

        log.info("넘어온 nickname 정보: {}", nickname);

        Optional<Member> member = memberService.checkExistNickname(nickname);

        return member.isPresent() ?
                ResponseDto.of(Code.NICKNAME_EXIST, nickname) : ResponseDto.of(Code.NICKNAME_OK, nickname);
    }
    
}
