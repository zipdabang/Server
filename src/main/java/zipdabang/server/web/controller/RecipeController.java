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
import org.springframework.web.bind.annotation.*;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.domain.member.Member;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "레시피 관련 API", description = "레시피 관련 API 모음입니다.")
public class
RecipeController {

    /*
    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @PostMapping(value = "/members/recipes",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> createRecipe(@ModelAttribute RecipeRequestDto.CreateRecipeDto request, @AuthMember Member member){
        return null;
    }
     */

    @Operation(summary = "🍹figma 레시피 작성하기1, 레시피 등록 API 🔑", description = "레시피 (작성)등록 화면 API입니다. 임시저장 api는 별도로 있음. step이랑 ingredient 몇개 들어오는지 각Count에 적어주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4006",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4007",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4010",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4013",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4100", description = "레시피 작성시 누락된 내용이 있습니다. 미완료는 임시저장으로 가세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseDto<RecipeResponseDto.CreateRecipeDto> createRecipe(@ModelAttribute RecipeRequestDto.CreateRecipeDto request, @AuthMember Member member){
        return null;
    }

    @Operation(summary = "🍹figma 레시피 상세페이지, 레시피 상세 정보 조회 API 🔑", description = "레시피 조회 화면 API입니다. 댓글은 처음 10개만 가져오고 나머지는 댓글 page api 드림")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4006",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4007",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4010",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4013",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/{recipeId}")
    public ResponseDto<RecipeResponseDto.RecipeInfoDto> recipeDetail(@PathVariable(name = "recipeId") Long recipeId, @AuthMember Member member) {
        return null;
    }

    @Operation(summary = "🍹figma 레시피2, 레시피 검색 목록조회 화면 API 🔑", description = "검색한 레시피 조회 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4006",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4007",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4010",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4013",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4018",description = "BAD_REQUEST, 페이지 번호 이상함, -1 이런거 주지 마세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 안주면 0으로(최초 페이지) 설정함, -1 이런거 주면 에러 뱉음"),
            @Parameter(name = "value", description = "query string 검색할 단어")
    })
    @GetMapping(value = "/members/recipes/search")
    public ResponseDto<RecipeResponseDto.RecipeSearchDto> searchRecipe(@RequestParam(name = "value") String value, @AuthMember Member member){
        return null;
    }
}
