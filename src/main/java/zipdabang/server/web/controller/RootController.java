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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.base.Code;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.converter.RootConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.service.RootService;
import zipdabang.server.web.dto.common.BaseDto;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
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
        return null;
    }


    @Operation(summary = "자동 로그인 API ✔️", description = "자동 로그인 API 입니다. 스웨거에 authorizationHeader는 무시해주세요 스웨거에서는 옆 자물쇠에 토큰 넣어주세요! 평소대로 헤더에 토큰 넣어서 주시면 됩니다")

    @Parameters({
            @Parameter(name = "user", hidden = true)
    })
    @GetMapping("/auto-login")
    public ResponseDto<BaseDto.BaseResponseDto> autoLogin(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){
        Boolean autoResult = rootService.autoLoginService(authorizationHeader);
        if(autoResult)
            return ResponseDto.of(Code.AUTO_LOGIN_MAIN,null);
        else
            return ResponseDto.of(Code.AUTO_LOGIN_NOT_MAIN,null);
    }
}
