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
import org.springframework.data.domain.Page;
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
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Follow;
import zipdabang.server.domain.member.Inquery;
import zipdabang.server.domain.member.Member;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.redis.service.RedisService;
import zipdabang.server.service.MemberService;
import zipdabang.server.sms.service.SmsService;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.validation.annotation.CheckPage;
import zipdabang.server.validation.annotation.CheckTempMember;
import zipdabang.server.validation.annotation.CheckDeregister;
import zipdabang.server.validation.annotation.ExistMember;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import org.springframework.web.bind.annotation.*;
import zipdabang.server.sms.dto.SmsResponseDto;
import zipdabang.server.utils.dto.OAuthResult;

import javax.validation.Valid;
import java.io.IOException;
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
@ApiResponses({
        @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
})
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
            @ApiResponse(responseCode = "2000", description = "OK 성공, 로그아웃, access token + refresh 토큰 버려주세요"),
    })
    @PostMapping("/members/logout")
    public ResponseDto<MemberResponseDto.MemberStatusDto> logout(@AuthMember Member member, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        memberService.logout(token, member);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "logout"));
    }

    //소셜로그인

    @Operation(summary = "🎪figma[온보딩1] 소셜로그인 API ✔️", description = "소셜로그인 API, 응답으로 로그인(메인으로 이동), 회원가입(정보 입력으로 이동) code로 구분하며 query String으로 카카오인지 구글인지 주면 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2050", description = "OK, 로그인, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "2051", description = "OK, 회원가입, 디비에 유저정보 저장 X, 만약 회원정보 입력하다가 도망가면 그냥 처음부터 다시 할 것"),
    })
    @Parameters({
            @Parameter(name = "type", description = "쿼리 스트링, 어떤 소셜로그인이지", required = true)
    })
    @PostMapping("/members/oauth")
    public ResponseDto<MemberResponseDto.SocialLoginDto> oauthKakao(
            @RequestBody MemberRequestDto.OAuthRequestDto oAuthRequestDto, @RequestParam(name = "type") String type) {
        OAuthResult.OAuthResultDto oAuthResultDto = memberService.SocialLogin(oAuthRequestDto, type);
        MemberResponseDto.SocialLoginDto socialLoginDto = MemberConverter.toSocialLoginDto(oAuthResultDto.getAccessToken(), oAuthResultDto.getRefreshToken());
        return oAuthResultDto.getIsLogin() ? ResponseDto.of(Code.OAUTH_LOGIN, socialLoginDto) : ResponseDto.of(Code.OAUTH_JOIN, null);
    }


    //회원 정보 추가입력 = 회원가입 완료 + 로그인
    @Operation(summary = "🎪figma[회원가입 까지 페이지 -  회원가입 완료 시] 소셜 회원가입 최종 완료 API ✔️", description = "소셜로그인을 통한 회원가입 최종완료 API입니다. agreeTermsIdList는 동의 한(선택 약관 중) 약관의 Id를 주세요 약관의 Id는 약관 조회 API에서 준 데이터에서 가져오세요")
    @Parameters({
            @Parameter(name = "type", description = "kakao or google을 쿼리 스트링으로 소문자로만 필수로 주면 됨")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4053", description = "BAD_REQUEST, 선호하는 음료 카테고리 id가 이상할 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
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
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 인증번호 전송 완료"),
            @ApiResponse(responseCode = "2054", description = "OK 성공 , 이미 회원가입된 전화번호입니다."),
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
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 인증 성공"),
            @ApiResponse(responseCode = "4056", description = "BAD_REQUEST , 전화번호를 잘못 전달했거나, 인증요청을 하지않은 상태로 확인버튼을 누른 경우"),
            @ApiResponse(responseCode = "4057", description = "BAD_REQUEST, 인증 번호가 옳지 않습니다."),
            @ApiResponse(responseCode = "4058", description = "BAD_REQUEST, 인증 시간(5분)이 지난 경우"),
    })
    @PostMapping("/members/phone/auth")
    public ResponseDto<SmsResponseDto.AuthNumResultDto> authPhoneNum(@RequestBody MemberRequestDto.PhoneNumAuthDto request) {
        SmsResponseDto.AuthNumResultDto authNumResultDto = smsService.authNumber(request.getAuthNum(), request.getPhoneNum());
        return ResponseDto.of(authNumResultDto.getResponseCode(), authNumResultDto);
    }


    // 내 선호 음료 조회
    @Operation(summary = "[figma 더보기 - 즐겨마시는 음료 종류 1] 유저 선호 카테고리 조회 API ✔️🔑", description = "유저 선호 카테고리 조회 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 유저 선호 카테고리 조회 완료"),
    })
    @GetMapping("/members/category")
    public ResponseDto<MemberResponseDto.MemberPreferCategoryDto> memberPreferCategories(@AuthMember Member member) {
        List<Category> categories = memberService.findMemberPreferCategories(member);

        return ResponseDto.of(MemberConverter.toMemberPreferCategoryDto(categories));
    }


    // 회원정보 조회 및 수정 APIs


    @Operation(summary = "[figma 더보기 - 회원 정보 1] 회원정보 조회 API ✔️🔑", description = "회원정보 조회 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 회원정보 조회 완료"),
    })
    @GetMapping("/myInfo")
    public ResponseDto<MemberResponseDto.MemberInfoResponseDto> showMyInfo(@AuthMember Member member) {
        List<Category> memberPreferCategories = memberService.findMemberPreferCategories(member);
        MemberResponseDto.MemberPreferCategoryDto memberPreferCategoryDto = MemberConverter.toMemberPreferCategoryDto(memberPreferCategories);
        return ResponseDto.of(MemberConverter.toMemberInfoDto(member, memberPreferCategoryDto));
    }

    @Operation(summary = "[figma 더보기 - 회원 정보 1] 프로필사진 수정 API ✔️🔑", description = "프로필사진 수정 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 프로필사진 수정 완료"),
    })
    @PatchMapping(value = "/myInfo/profileImage", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseDto<MemberResponseDto.MemberStatusDto> updateProfileImage(@AuthMember Member member, @ModelAttribute MemberRequestDto.changeProfileDto request) throws IOException {
        memberService.updateMemberProfileImage(member, request);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "updateProfileImage"));
    }

    @Operation(summary = "[figma 더보기 - 회원 정보 수정 1] 기본정보 수정 API ✔️🔑", description = "기본정보 수정 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 기본정보 수정 완료"),
    })
    @PatchMapping("/myInfo/basicInfo")
    public ResponseDto<MemberResponseDto.MemberStatusDto> updateBasicInfo(@AuthMember Member member, @RequestBody MemberResponseDto.MemberBasicInfoDto request) {
        memberService.updateMemberBasicInfo(member, request);

        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "updateBasicInfo"));
    }

    @Operation(summary = "[figma 더보기 - 회원 정보 수정 2] 상세정보 수정 API ✔️🔑", description = "상세정보 수정 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 상세정보 수정 완료"),
    })
    @PatchMapping("/myInfo/detailInfo")
    public ResponseDto<MemberResponseDto.MemberStatusDto> updateDetailInfo(@AuthMember Member member, @RequestBody MemberResponseDto.MemberDetailInfoDto request) {
        memberService.updateMemberDetailInfo(member, request);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "updateDetailInfo"));
    }

    @Operation(summary = "[figma 더보기 - 회원 정보 수정 3] 닉네임 수정 API ✔️🔑", description = "닉네임 수정 API입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 닉네임 수정 완료"),
    })
    @PatchMapping("/myInfo/nickname")
    public ResponseDto<MemberResponseDto.MemberStatusDto> updateNickname(@AuthMember Member member, @RequestBody MemberRequestDto.changeNicknameDto request) {
        memberService.updateMemberNickname(member, request.getNickname());
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "updateNickname"));
    }


    // 내 선호 음료 카테고리 수정
    @Operation(summary = "[figma 더보기 - 즐겨마시는 음료 종류 1] 유저 선호 카테고리 수정 API ✔️🔑", description = "유저 선호 카테고리 수정 API입니다. 카테고리명(커피, 차 등)을 넣으시면 됩니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 , 유저 선호 카테고리 수정 완료"),
    })
    @PatchMapping("/myInfo/category")
    public ResponseDto<MemberResponseDto.MemberStatusDto> updatePreferCategories(@AuthMember Member member, @RequestBody MemberRequestDto.changeCategoryDto request) {
        List<String> categories = request.getCategories();
        memberService.updateMemberPreferCategory(member, request);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "updatePreferCategories"));
    }


    //닉네임 중복검사
    @Operation(summary = "🎪[figma 회원가입까지 - 닉네임 입력 1,2,3] 닉네임 중복검사 API ✔️", description = "닉네임 중복검사 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2052", description = "OK 성공 , 닉네임 존재함 다시 시도하세요"),
            @ApiResponse(responseCode = "2053", description = "OK 성공 , 닉네임 사용 가능"),
    })
    @GetMapping("/members/exist-nickname")

    public ResponseDto<String> checkExistNickname(@RequestParam String nickname) {

        log.info("넘어온 nickname 정보: {}", nickname);

        Optional<Member> member = memberService.checkExistNickname(nickname);

        return member.isPresent() ?
                ResponseDto.of(Code.NICKNAME_EXIST, nickname) : ResponseDto.of(Code.NICKNAME_OK, nickname);
    }

    @Operation(summary = "리프레쉬 토큰을 이용해 accessToken 재발급 API ✔️", description = "리프레쉬 토큰을 이용해 accessToken 재발급하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4050", description = "BAD_REQEUST , refresh token이 서버로 넘어오지 않음", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @PostMapping("/members/new-token")
    public ResponseDto<MemberResponseDto.IssueNewTokenDto> getNewToken(MemberRequestDto.IssueTokenDto request) {
        RefreshToken newRefreshToken = redisService.reGenerateRefreshToken(request);
        String accessToken = memberService.regenerateAccessToken(newRefreshToken);
        return ResponseDto.of(MemberConverter.toIssueNewTokenDto(accessToken, newRefreshToken.getToken()));
    }

    @GetMapping("/members/test")
    public String test() {
        return "test!";
    }

    @Operation(summary = "🎪figma[회원가입 까지 페이지 - 이용약관] 이용약관 조회 API ✔️", description = "이용약관 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4050", description = "BAD_REQEUST , refresh token이 서버로 넘어오지 않음", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/terms")
    public ResponseDto<MemberResponseDto.TermsListDto> showTerms() {
        return ResponseDto.of(MemberConverter.toTermsDto(memberService.getAllTerms()));
    }


    @Operation(summary = "🎪figma[온보딩1] 나중에 로그인하기 API ✔️", description = "나중에 로그인하기 API 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token 하나만 반환함"),
    })
    @PostMapping("/members/temp-login")
    public ResponseDto<MemberResponseDto.TempLoginDto> tempLogin() {
        return ResponseDto.of(MemberConverter.toTempLoginDto(memberService.tempLoginService()));
    }

    @Operation(summary = "🎪figma[더보기 - 오류 신고 및 신고하기] 오류 신고하기 API ✔️🔑", description = "오류 신고하기 API 입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/inquiries",consumes ={ MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ResponseDto<MemberResponseDto.MemberInqueryResultDto> createInquery(@CheckTempMember @AuthMember Member member, @ModelAttribute @Valid MemberRequestDto.InqueryDto request){
        Inquery inquery = memberService.createInquery(member, request);
        return ResponseDto.of(MemberConverter.toMemberInqueryResultDto(inquery));
    }

    @Operation(summary = "🎪[더보기 - 오류 신고및 신고하기5] 내가 문의 한 오류 모아보기 (페이징 포함) ✔️🔑", description = "내가 신고한 오류 모아보기 입니다.")
    @GetMapping("/members/inquiries")
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "page", description = "페이지 번호, 1부터 시작")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "4054", description = "BAD_REQEUST , 페이지 번호가 없거나 0 이하", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQEUST , 페이지 번호가 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),

    })
    public ResponseDto<MemberResponseDto.InqueryListDto> showInquery(@CheckTempMember @AuthMember Member member, @RequestParam(name = "page",required = true) @CheckPage Integer page){
        Page<Inquery> inqueryPage = memberService.findInquery(member, page);
        return ResponseDto.of(MemberConverter.toInqueryListDto(inqueryPage));
    }
    @Operation(summary = "[figma 더보기 - 회원 탈퇴] 회원 탈퇴 API ✔️🔑", description = "회원 탈퇴 API입니다.<br> 테스트를 위해 임시로 해당 유저의 상세주소를 \"TEST\" 로 설정하면(상세정보 수정 API - zipCode) 탈퇴 불가능한 경우로 처리되도록 해놨습니다.<br> deregisterTypes 종류 <br>"+
            "- NOTHING_TO_BUY(\"사고싶은 물건이 없어요.\"),<br>" +
            "- DISINTERESTED(\"앱을 이용하지 않아요.\"),<br>" +
            "- UNCOMFORTABLE(\"앱 이용이 불편해요.\"),<br>" +
            "- NEW_REGISTER(\"새 계정을 만들고 싶어요.\"),<br>" +
            "- MET_RUDE_USER(\"비매너 유저를 만났어요.\"),<br>" +
            "- OTHERS(\"기타\")")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, 유저 비활성화 완료"),
            @ApiResponse(responseCode = "4061", description = "탈퇴할 수 없는 유저입니다. 탈퇴 불가 사유가 존재합니다."),
    })
    @PatchMapping("/members/deregister")
    public ResponseDto<MemberResponseDto.MemberStatusDto> deregister(@CheckDeregister @AuthMember Member member, MemberRequestDto.DeregisterDto request) {
        memberService.memberDeregister(member, request);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "deregister"));

    }


    @Operation(summary = "유저 차단 API ✔️🔑", description = "유저 차단 API 입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, 유저 차단 완료"),
            @ApiResponse(responseCode = "4052", description = "해당 사용자가 존재하지 않습니다"),
            @ApiResponse(responseCode = "4062", description = "이미 차단된 사용자입니다."),
            @ApiResponse(responseCode = "4063", description = "자신을 차단할 수 없습니다."),
    })
    @PostMapping("/members/block")
    public ResponseDto<MemberResponseDto.MemberStatusDto> block(@AuthMember Member member, Long blocked) {
        memberService.blockMember(member, blocked);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "Block"));
    }

    @Operation(summary = "유저 차단 해지 API ✔️🔑", description = "유저 차단 해지 API 입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, 유저 차단 해지 완료"),
            @ApiResponse(responseCode = "4052", description = "해당 사용자가 존재하지 않습니다"),
    })
    @DeleteMapping("/members/unblock")
    public ResponseDto<MemberResponseDto.MemberStatusDto> unblock(@AuthMember Member member, Long blocked) {
        memberService.unblockMember(member, blocked);
        return ResponseDto.of(MemberConverter.toMemberStatusDto(member.getMemberId(), "Unblock"));
    }

    @Operation(summary = "차단 유저 목록 조회 API 🔑", description = "차단 유저 목록 조회 API 입니다.")
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "page", description = "페이지 번호, 1부터 시작")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, 차단 유저 목록 조회 완료"),
            @ApiResponse(responseCode = "4052", description = "해당 사용자가 존재하지 않습니다"),
            @ApiResponse(responseCode = "4054", description = "BAD_REQUEST , 페이지 번호가 없거나 0 이하", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST , 페이지 번호가 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/blockedList")
    public ResponseDto<MemberResponseDto.PagingMemberListDto> blockerMemberList(@RequestParam(name = "page", required = false) Integer page, @AuthMember Member member) {
        if (page == null)
            page = 1;
        else if (page < 1)
            throw new MemberException(Code.UNDER_PAGE_INDEX_ERROR);
        page -= 1;

        Page<Member> blockedMembers = memberService.findBlockedMember(page, member);
        return ResponseDto.of(MemberConverter.toPagingMemberListDto(blockedMembers));
    }


    @Operation(summary = "🎪팔로우하기 API", description = "팔로우하기 API 입니다.")
    @PostMapping("/members/followings/{targetId}")
    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4064", description = "BAD_REQEUST , 팔로우하려는 대상이 없음", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4065", description = "FORBIDDEN , 스스로는 팔로우가 안됩니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    public ResponseDto<MemberResponseDto.FollowingResultDto> followMember(@CheckTempMember @AuthMember Member member, @ExistMember @PathVariable(name = "targetId") Long targetId){
        Follow follow = memberService.createFollow(targetId, member);
        return ResponseDto.of(MemberConverter.toFollowingResultDto(follow));
    }

    @Operation(summary = "🎪팔로우중인 사용자 조회 API", description = "팔로우중인 사용자 조회 API 입니다. 페이지 주세요")
    @GetMapping("/members/followings")
    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4054", description = "BAD_REQUEST , 페이지 번호가 없거나 0 이하", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST , 페이지 번호가 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    public ResponseDto<MemberResponseDto.FollowingListDto> getFollowingMember(@CheckPage Integer page, @CheckTempMember @AuthMember Member member){
        Page<Follow> following = memberService.findFollowing(member, page);
        return ResponseDto.of(MemberConverter.toFollowingListDto(following));
    }
}
