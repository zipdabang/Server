package zipdabang.server.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import zipdabang.server.FeignClient.service.KakaoOauthService;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.base.Code;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.redis.service.RedisService;
import zipdabang.server.service.MemberService;
import zipdabang.server.sms.service.SmsService;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.dto.OAuthResult;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "유저 관련 API", description = "로그인, 회원가입, 마이 페이지에서 필요한 API모음")
public class MemberRestController {

    private final MemberService memberService;
    private final SmsService smsService;

    private final KakaoOauthService kakaoOauthService;

    private final RedisService redisService;

    @Operation(summary = "로그아웃 API", description = "로그아웃 API 입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "Authorization", description = "swagger에서 나오는 이건 무시하고 오른쪽 위의 자물쇠에 토큰 넣어서 테스트 하세요")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공, 로그아웃, access toekn + refresh 토큰 버려주세요"),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),

    })
    @PostMapping("/members/logout")
    public ResponseDto<MemberResponseDto.MemberStatusDto> logout(@AuthMember Member member, @RequestHeader(value = "Authorization",required = false) String authorizationHeader, @RequestBody MemberRequestDto.LogoutDto request) {
        String token = authorizationHeader.substring(7);
        memberService.logout(token,request);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "logout"));
    }

    @PatchMapping("/members/quit")
    public ResponseDto<MemberResponseDto.MemberStatusDto> quit(@RequestBody MemberRequestDto.quitMember request) {
        return null;
    }

    @PatchMapping("/members/restore")
    public ResponseDto<MemberResponseDto.MemberStatusDto> restore(@RequestBody MemberRequestDto.restoreMember request) {
        return null;
    }

    //소셜로그인

    @Operation(summary = "🎪figma[온보딩1] 소셜로그인 API ✔️", description = "소셜로그인 API, 응답으로 로그인(메인으로 이동), 회원가입(정보 입력으로 이동) code로 구분하며 query String으로 카카오인지 구글인지 주면 됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "2010",description = "OK, 로그인, access Token과 refresh 토큰을 반환함"),
        @ApiResponse(responseCode = "2011",description = "OK, 회원가입, 디비에 유저정보 저장 X, 만약 회원정보 입력하다가 도망가면 그냥 처음부터 다시 할 것"),
        @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "type",description = "쿼리 스트링, 어떤 소셜로그인이지", required = true)
    })
    @PostMapping("/members/oauth")
    public ResponseDto<MemberResponseDto.SocialLoginDto> oauthKakao(
            @RequestBody MemberRequestDto.OAuthRequestDto oAuthRequestDto, @RequestParam(name = "type") String type) {
        OAuthResult.OAuthResultDto oAuthResultDto = memberService.SocialLogin(oAuthRequestDto, type);
        MemberResponseDto.SocialLoginDto socialLoginDto = MemberConverter.toSocialLoginDto(oAuthResultDto.getAccessToken(),oAuthResultDto.getRefreshToken());
        return oAuthResultDto.getIsLogin() ? ResponseDto.of(Code.OAUTH_LOGIN,socialLoginDto) : ResponseDto.of(Code.OAUTH_JOIN,null);
    }

    @GetMapping("/members/category")
    public ResponseDto<List<Category>> getCategoryList() {
        List<Category> categoryList = memberService.getCategoryList();

        log.info("음료 카테고리 리스트: {}", categoryList);
        return ResponseDto.of(categoryList);
    }

    //회원 정보 추가입력 = 회원가입 완료 + 로그인
    @Operation(summary = "🎪figma[회원가입 까지 페이지 -  회원가입 완료 시] 소셜 회원가입 최종 완료 API ✔️", description = "소셜로그인을 통한 회원가입 최종완료 API입니다. agreeTermsIdList는 동의 한(선택 약관 중) 약관의 Id를 주세요 약관의 Id는 약관 조회 API에서 준 데이터에서 가져오세요")
    @Parameters({
            @Parameter(name = "type", description = "kakao or google을 쿼리 스트링으로 소문자로만 필수로 주면 됨")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4017", description = "BAD_REQEUST, 선호하는 음료 카테고리 id가 이상할 경우",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @PostMapping("/members/oauth/info")
    public ResponseDto<MemberResponseDto.SocialJoinDto> memberInfoForSignUp(@RequestBody MemberRequestDto.MemberInfoDto request, @RequestParam(name = "type", required = true) String type) {
        log.info("body로 넘겨온 사용자 정보: {}", request.toString());
        OAuthJoin.OAuthJoinDto oAuthJoinDto = memberService.joinInfoComplete(request, type);
        return ResponseDto.of(MemberConverter.toSocialJoinDto(oAuthJoinDto));
    }

    //인증번호 요청
    @Operation(summary = "🎪figma[회원가입 까지 페이지 -  회원정보 입력] 인증번호 요청 API ✔️️", description = "인증번호 요청 API입니다. 대시(-) 제외 전화번호 입력하시면 됩니다. ex) 01012345678 ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공 , 인증번호 전송 완료"),
            @ApiResponse(responseCode = "2020",description = "OK 성공 , 이미 회원가입된 전화번호입니다."),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @PostMapping("/members/phone/sms")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> sendSms(@RequestBody MemberRequestDto.SmsRequestDto request) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        memberService.existByPhoneNumber(request.getTargetPhoneNum());
        SmsResponseDto.AuthNumResultDto authNumResultDto = smsService.sendSms(request.getTargetPhoneNum());
        return ResponseDto.of(authNumResultDto);
    }

    //인증번호 검증
    @Operation(summary = "🎪figma[회원가입 까지 페이지 -  회원정보 입력] 인증번호 검증 API ✔️️", description = "인증번호 검증 API입니다. 대시(-) 제외 전화번호와 인증번호 입력하시면 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공 , 인증 성공"),
            @ApiResponse(responseCode = "4200",description = "BAD_REQUEST , 전화번호를 잘못 전달했거나, 인증요청을 하지않은 상태로 확인버튼을 누른 경우"),
            @ApiResponse(responseCode = "4201",description = "BAD_REQUEST, 인증 번호가 옳지 않습니다."),
            @ApiResponse(responseCode = "4202",description = "BAD_REQUEST, 인증 시간(5분)이 지난 경우"),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping("/members/phone/auth")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> authPhoneNum(@RequestBody MemberRequestDto.PhoneNumAuthDto request) {
        SmsResponseDto.AuthNumResultDto authNumResultDto = smsService.authNumber(request.getAuthNum(), request.getPhoneNum());
        return ResponseDto.of(authNumResultDto.getResponseCode(), authNumResultDto);
    }


    //프로필 수정
    @PatchMapping(value = "/members",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ResponseDto<MemberResponseDto.MemberStatusDto> updateProfile (@ModelAttribute MemberRequestDto.memberProfileDto request )
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

    @Operation(summary = "🎪[figma 회원가입까지 - 닉네임 입력 1,2,3] 닉네임 중복검사 API ✔️", description = "닉네임 중복검사 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2010",description = "OK 성공 , 닉네임 존재함 다시 시도하세요"),
            @ApiResponse(responseCode = "2011",description = "OK 성공 , 닉네임 사용 가능"),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/exist-nickname")
    public ResponseDto<String> checkExistNickname (@RequestParam String nickname){

        log.info("넘어온 nickname 정보: {}", nickname);

        Optional<Member> member = memberService.checkExistNickname(nickname);

        return member.isPresent() ?
                ResponseDto.of(Code.NICKNAME_EXIST, nickname) : ResponseDto.of(Code.NICKNAME_OK, nickname);
    }

    @Operation(summary = "리프레쉬 토큰을 이용해 accessToken 재발급 API ✔️", description = "리프레쉬 토큰을 이용해 accessToken 재발급하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4014",description = "BAD_REQEUST , refresh token이 서버로 넘어오지 않음",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @PostMapping("/members/new-token")
    public ResponseDto<MemberResponseDto.IssueNewTokenDto> getNewToken(MemberRequestDto.IssueTokenDto request){
        RefreshToken newRefreshToken = redisService.reGenerateRefreshToken(request);
        String accessToken = memberService.regenerateAccessToken(newRefreshToken);
        return ResponseDto.of(MemberConverter.toIssueNewTokenDto(accessToken, newRefreshToken.getToken()));
    }

    @GetMapping("/members/test")
    public String test(){
        return "test!";
    }

    @Operation(summary = "🎪figma[회원가입 까지 페이지 - 이용약관] 이용약관 조회 API ✔️", description = "이용약관 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4014",description = "BAD_REQEUST , refresh token이 서버로 넘어오지 않음",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/terms")
    public ResponseDto<MemberResponseDto.TermsListDto> showTerms(){
        return ResponseDto.of(MemberConverter.toTermsDto(memberService.getAllTerms()));
    }
}
