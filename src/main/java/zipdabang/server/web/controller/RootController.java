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
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.converter.RootConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.service.RootService;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "홈 API", description = "홈 화면, 그리고 기타 API 모음집입니다.")
public class RootController {

    private final RootService rootService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @Operation(summary = "음료 카테고리 조회 API ✔️", description = "음료 카테고리 조회 API입니다. 추후 응답에 있는 id는 회원가입 때 사용됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공, access Token과 refresh 토큰을 반환함"),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/categories")
    public ResponseDto<RootResponseDto.BeverageCategoryListDto> showCategoryList(){
        List<Category> allCategories = rootService.getAllCategories();
        return ResponseDto.of(RootConverter.toBeverageCategoryListDto(allCategories));
    }
}
