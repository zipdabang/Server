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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.reponse.ResponseDto;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.converter.RootConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.inform.HomeBanner;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.RootService;
import zipdabang.server.validation.annotation.CheckTempMember;
import zipdabang.server.validation.annotation.ExistNotification;
import zipdabang.server.validation.annotation.ExistPushAlarm;
import zipdabang.server.web.dto.common.BaseDto;
import zipdabang.server.web.dto.requestDto.RootRequestDto;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import java.io.IOException;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
@ApiResponses({
        @ApiResponse(responseCode = "2000",description = "OK 성공"),
        @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
})
@Tag(name = "홈 API", description = "홈 화면, 그리고 기타 API 모음집입니다.")
public class RootController {

    private final RootService rootService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @Operation(summary = "음료 카테고리 조회 API ✔️", description = "음료 카테고리 조회 API입니다. 추후 응답에 있는 id는 회원가입 때 사용됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공, access Token과 refresh 토큰을 반환함"),
    })
    @GetMapping("/categories")
    public ResponseDto<RootResponseDto.BeverageCategoryListDto> showCategoryList(){
        List<Category> allCategories = rootService.getAllCategories();
        return ResponseDto.of(RootConverter.toBeverageCategoryListDto(allCategories));
    }

    @Operation(summary = "배너 이미지 API 🔑", description = "홈 화면의 배너 이미지를 가져옵니다. order는 배너 순서를 의미합니다.")
    @GetMapping("/banners")
    public ResponseDto<RootResponseDto.BannerImageDto> showBanners() {
        List<HomeBanner> bannerList = rootService.getBannerList();

        return ResponseDto.of(RootConverter.toRecipeBannerImageDto(bannerList));
    }


    @Operation(summary = "자동 로그인 API ✔️", description = "자동 로그인 API 입니다. 스웨거에 authorizationHeader는 무시해주세요 스웨거에서는 옆 자물쇠에 토큰 넣어주세요! 평소대로 헤더에 토큰 넣어서 주시면 됩니다")

    @Parameters({
            @Parameter(name = "user", hidden = true)
    })
    @GetMapping("/auto-login")
    public ResponseDto<BaseDto.BaseResponseDto> autoLogin(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        log.info("프론트가 보낸토큰 : {}", authorizationHeader);
        Boolean autoResult = rootService.autoLoginService(authorizationHeader);
        if(autoResult)
            return ResponseDto.of(CommonStatus.AUTO_LOGIN_MAIN,null);
        else
            return ResponseDto.of(CommonStatus.AUTO_LOGIN_NOT_MAIN,null);
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseDto<RootResponseDto.NoticeSpecDto> showNotification(@PathVariable(name = "noticeId") @ExistNotification Long noticeId){
        Notification notification = rootService.findNotification(noticeId);
        return ResponseDto.of(RootConverter.toNoticeSpecDto(notification));
    }

    @Operation(summary = "[🎪figma 더보기-공지사항1] 공지 목록 조회 API",description = "공지사항 목록 조회 API 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공 공지를 최신순으로 보여줌"),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/notices")
    public ResponseDto<RootResponseDto.NoticeListDto> getNoticeList(){
        List<Notification> notificationList = rootService.notificationList();
        return ResponseDto.of(RootConverter.toNoticeListDto(notificationList));
    }

    @Operation(summary = "신고 목록 조회 API 🔑 ✔", description = "신고 목록 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/reports")
    public ResponseDto<RootResponseDto.ReportListDto> showReportList(){
        List<Report> allReports = rootService.getAllReports();
        return ResponseDto.of(RootConverter.toReportListDto(allReports));
    }

    @Operation(summary = "FCM 테스트 API", description = "테스트용")
    @PostMapping("/fcm")
    public ResponseDto<BaseDto> testFCM(@RequestBody RootRequestDto.FCMTestDto fcmToken) throws IOException
    {
        rootService.testFCMService(fcmToken.getFcmToken());
        return ResponseDto.of(null);
    }


    @Operation(summary = "푸쉬알림 읽음처리 API ✔️🔑", description = "푸쉬알림 읽음처리 API")
    @DeleteMapping("/push-alarm/{alarmId}")
    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4011", description = "NOT_FOUND, 푸쉬알림이 없습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4012", description = "FORBIDDEN, 내 푸쉬알림이 아닙니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    public ResponseDto<RootResponseDto.ReadPushAlarm> readPushAlarm(@CheckTempMember @AuthMember Member member,@ExistPushAlarm @PathVariable(name = "alarmId") Long alarmId)
    {
        rootService.readPushAlarm(alarmId);
        return ResponseDto.of(RootConverter.toReadPushAlarm());
    }


//    @Operation(summary = "닉네임 필터링용 엑셀 파싱 API ✔️", description = "닉네임 필터링용 엑셀 파싱 API")
//    @PostMapping(value = "/excel", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @ApiResponses({
//            @ApiResponse(responseCode = "2000", description = "OK 성공"),
//    })
//    public ResponseDto<RootResponseDto.ExcelParsingDto> parsingExcelFile(@ModelAttribute MultipartFile file) throws IOException{
//        rootService.ParseExcelFile(file);
//        return null;
//    }
}
