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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.apiPayload.reponse.ResponseDto;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.web.dto.responseDto.MarketResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "마켓 관련 API", description = "마켓 관련 API 모음입니다.")
public class MarketController {

    @Operation(summary = "🎪figma 마켓1, 내가 조회한 아이템 목록 미리보기 화면 API 🔑", description = "내가 조회한 아이템 목록 미리보기 화면 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2150",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @GetMapping("/market/recent-items")
    public ResponseDto<MarketResponseDto.WatchedProductPreviewDto> getWatchedPreview(@AuthMember Member member){
        return null;
    }

    @Operation(summary = "🎪figma 마켓2, 내가 조회한 아이템 목록조회 화면 API 🔑", description = "내가 조회한 아이템 목록 목록조회 화면 API입니다. pageIndex로 페이징 함")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2150",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4053",description = "BAD_REQUEST, 카테고리 이상함",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054",description = "BAD_REQUEST, 페이지 번호 이상함, -1 이런거 주지 마세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "categoryId", description = "path variable 카테고리 아이디, 0이면 전체"),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 안주면 0으로(최초 페이지) 설정함, -1 이런거 주면 에러 뱉음"),
    })
    @GetMapping("/market/recent-items/{categoryId}")
    public ResponseDto<MarketResponseDto.WatchedProductDto> getWatched(@AuthMember Member member, @RequestParam(name = "pageIndex", required = false) Integer pageIndex, @PathVariable(name = "categoryId") Long categoryId){
        return null;
    }
}
