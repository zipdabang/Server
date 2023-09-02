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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.base.Code;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.base.exception.handler.RecipeException;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.converter.RootConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "레시피 관련 API", description = "레시피 관련 API 모음입니다.")
public class
RecipeController {

    private final RecipeService recipeService;

    @Operation(summary = "🍹figma 레시피 작성하기1, 레시피 등록 API 🔑 ✔", description = "레시피 (작성)등록 화면 API입니다. 임시저장 api는 별도로 있음. step이랑 ingredient 몇개 들어오는지 각Count에 적어주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4100", description = "레시피 작성시 누락된 내용이 있습니다. 미완료는 임시저장으로 가세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> createRecipe(
            @RequestPart(value = "content") RecipeRequestDto.CreateRecipeDto request,
            @RequestPart(value = "thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "stepImages") List<MultipartFile> stepImages,
            @AuthMember Member member) throws IOException {

        log.info("사용자가 준 정보 : {}", request.toString());

        Recipe recipe = recipeService.create(request, thumbnail, stepImages, member);
        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "🍹figma 레시피 상세페이지, 레시피 상세 정보 조회 API 🔑 ✔", description = "레시피 조회 화면 API입니다. 댓글은 처음 10개만 가져오고 나머지는 댓글 page api 드림")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101",description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4102",description = "BAD_REQUEST, 차단한 사용자의 recipe 입니다. 접근할 수 없습니다.",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/{recipeId}")
    public ResponseDto<RecipeResponseDto.RecipeInfoDto> recipeDetail(@PathVariable(name = "recipeId") Long recipeId, @AuthMember Member member) {

        Recipe recipe = recipeService.getRecipe(recipeId, member);
        Boolean isOwner = recipeService.checkOwner(recipe, member);
        Boolean isLiked = recipeService.getLike(recipe, member);
        Boolean isScrapped = recipeService.getScrap(recipe, member);

        recipe.updateTotalView();

        return ResponseDto.of(RecipeConverter.toRecipeInfoDto(recipe, isOwner, isLiked, isScrapped, member));
    }

    @Operation(summary = "🍹figma 나의 레시피 삭제_알럿, 레시피 삭제 API 🔑", description = "레시피 삭제 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 삭제처리 되었습니다."),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101",description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @DeleteMapping("/members/recipes/{recipeId}")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> deleteRecipe(@PathVariable(name = "recipeId") Long recipeId, @AuthMember Member member){
        return null;
    }


    @Operation(summary = "🍹figma 레시피2, 레시피 검색 목록조회 화면 API 🔑 ✔", description = "검색한 레시피 조회 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054",description = "BAD_REQUEST, 페이지 번호 0 이하입니다. 1 이상으로 주세요.",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055",description = "BAD_REQUEST, 페이지 인덱스 범위 초과함",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 안주면 1으로(최초 페이지) 설정함, 0 이런거 주면 에러 뱉음"),
            @Parameter(name = "keyword", description = "query string 검색할 단어")
    })
    @GetMapping(value = "/members/recipes/search")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> searchRecipe(@RequestParam(name = "keyword", required = false) String keyword, @RequestParam(name = "pageIndex", required = false) Integer pageIndex, @AuthMember Member member){

        if(pageIndex == null)
            pageIndex =1;
        else if (pageIndex < 1)
            throw new RecipeException(Code.UNDER_PAGE_INDEX_ERROR);

        pageIndex -= 1;

        Page<Recipe> recipes= recipeService.searchRecipe(keyword,pageIndex,member);

        log.info(recipes.toString());

        if(recipes.getTotalElements() == 0)
            throw new RecipeException(Code.RECIPE_NOT_FOUND);
        if(pageIndex >= recipes.getTotalPages())
            throw  new RecipeException(Code.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피2, 카테고리 별 레시피 목록 조회 API 🔑", description = "카테고리 별 레시피 목록 조회 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4053",description = "BAD_REQUEST, 넘겨받은 categoryId와 일치하는 카테고리 없음. 1~6 사이로 보내세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054",description = "BAD_REQUEST, 페이지 번호 0 이하입니다. 1 이상으로 주세요.",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055",description = "BAD_REQUEST, 페이지 인덱스 범위 초과함",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4104",description = "BAD_REQUEST, 조회 방식 타입이 잘못되었습니다. likes, views, lastest중 하나로 보내주세요.",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 안주면 0으로(최초 페이지) 설정함, -1 이런거 주면 에러 뱉음"),
            @Parameter(name = "order", description = "query string 조회 방식. 인기순: likes, 조회순: views, 최신순: latest로 넘겨주세요, 기본값 latest")
    })
    @GetMapping(value = "/members/recipes/categories/{categoryId}")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> recipeListByCategory(@PathVariable Long categoryId, @RequestParam(name = "order", required = false) String order, @RequestParam(name = "pageIndex", required = false) Integer pageIndex, @AuthMember Member member){
        if(pageIndex == null)
            pageIndex =1;
        else if (pageIndex < 1)
            throw new RecipeException(Code.UNDER_PAGE_INDEX_ERROR);

        pageIndex -= 1;

        Page<Recipe> recipes = recipeService.recipeListByCategory(categoryId,pageIndex,member,order);


        log.info(recipes.toString());

        if(recipes.getTotalElements() == 0)
            throw new RecipeException(Code.RECIPE_NOT_FOUND);
        if(pageIndex >= recipes.getTotalPages())
            throw  new RecipeException(Code.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피1, 모든사람/인플루언서/우리들의 레시피 미리보기 API 🔑 ✔", description = "5개씩 미리보기로 가져오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4103",description = "BAD_REQUEST, 레시피 작성자 타입이 잘못되었습니다. all, influencer, common중 하나로 보내주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "writtenby", description = "query string 누가 쓴 레시피 종류인지. 모든 사람: all, 인플루언서: influencer, 우리들: common으로 넘겨주세요")
    })
    @GetMapping(value = "/members/recipes/types/preview")
    public ResponseDto<RecipeResponseDto.RecipeListDto> recipeListPreviewWrittenBy(@RequestParam(name = "writtenby") String writtenby, @AuthMember Member member){
        List<Recipe> recipes = recipeService.getWrittenByRecipePreview(writtenby, member);

        return ResponseDto.of(RecipeConverter.toPreviewRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피2, 모든사람/인플루언서/우리들의 레시피 목록 API 🔑", description = "레시피 목록 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4053",description = "BAD_REQUEST, 넘겨받은 categoryId와 일치하는 카테고리 없음. 1~6 사이로 보내세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054",description = "BAD_REQUEST, 페이지 번호 0 이하입니다. 1 이상으로 주세요.",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055",description = "BAD_REQUEST, 페이지 인덱스 범위 초과함",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "writtenby", description = "query string 누가 쓴 레시피 종류인지. 모든 사람: all, 인플루언서: influencer, 우리들: common으로 넘겨주세요"),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 안주면 0으로(최초 페이지) 설정함, -1 이런거 주면 에러 뱉음"),
            @Parameter(name = "order", description = "query string 조회 방식. 인기순: likes, 조회순: views, 최신순: latest로 넘겨주세요")
    })
    @GetMapping(value = "/members/recipes/types")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> recipeListWrittenBy(@RequestParam(name = "writtenby") String writtenby, @RequestParam(name = "order") String order, @RequestParam(name = "pageIndex", required = false) Integer pageIndex, @AuthMember Member member){
        return null;
    }

    @Operation(summary = "🏠figma 홈1, 주간 베스트 레시피 API 🔑", description = "이번 주 베스트 레시피 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100",description = "OK, 목록이 없을 경우, result = null",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/week-best")
    public ResponseDto<RecipeResponseDto.RecipeListDto> recipeWeekBest(@AuthMember Member member){
        return null;
    }

    @Operation(summary = "레시피 스크랩/취소 API 🔑 ✔", description = "레시피 스크랩/취소 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101",description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/{recipeId}/scrap")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> recipeScrapOrCancel(@PathVariable Long recipeId, @AuthMember Member member){
        Recipe recipe = recipeService.updateScrapOnRecipe(recipeId, member);

        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 좋아요/취소 API 🔑 ✔", description = "레시피 좋아요/취소 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101",description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/{recipeId}/likes")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> recipeLikeOrCancel(@PathVariable Long recipeId, @AuthMember Member member){

        Recipe recipe = recipeService.updateLikeOnRecipe(recipeId, member);

        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 배너 이미지 API 🔑", description = "레시피 화면의 배너 이미지를 가져옵니다. order는 배너 순서를 의미합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/recipes/banners")
    public ResponseDto<RecipeResponseDto.RecipeBannerImageDto> showBanners() {
        return null;
    }

    @Operation(summary = "레시피 카테고리 조회 API 🔑 ✔️", description = "레시피 카테고리 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000",description = "OK 성공"),
            @ApiResponse(responseCode = "4003",description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005",description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008",description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052",description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000",description = "SERVER ERROR, 백앤드 개발자에게 알려주세요",content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/recipes/categories")
    public ResponseDto<RecipeResponseDto.RecipeCategoryListDto> showCategoryList(){
        List<RecipeCategory> allCategories = recipeService.getAllRecipeCategories();
        return ResponseDto.of(RecipeConverter.RecipeCategoryListDto(allCategories));
    }
}
