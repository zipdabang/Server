package zipdabang.server.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "소셜로그인 API", description = "소셜로그인 API, 응답으로 로그인(메인으로 이동), 회원가입(정보 입력으로 이동) code로 구분하며 query String으로 카카오인지 구글인지 주면 됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "2010",description = "OK, 정상응답"),
        @ApiResponse(responseCode = "2011",description = "OK, 정상응답"),
        @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "type",description = "쿼리 스트링, 어떤 소셜로그인이지", required = true)
    })
    @PostMapping("/members/oauth")
    public ResponseDto<MemberResponseDto.SocialLoginDto> oauthKakao(
            @RequestBody MemberRequestDto.OAuthRequestDto oAuthRequestDto, @RequestParam(name = "type") String type) {
        OAuthResult.OAuthResultDto oAuthResultDto = memberService.kakaoSocialLogin(oAuthRequestDto.getEmail(), oAuthRequestDto.getProfileUrl(), type);
        MemberResponseDto.SocialLoginDto socialLoginDto = MemberConverter.toSocialLoginDto(oAuthResultDto.getJwt());
        return oAuthResultDto.getIsLogin() ? ResponseDto.of(Code.OAUTH_LOGIN,socialLoginDto) : ResponseDto.of(Code.OAUTH_JOIN,socialLoginDto);
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
