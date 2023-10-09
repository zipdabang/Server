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
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.code.RecipeStatus;
import zipdabang.server.apiPayload.exception.handler.MemberException;
import zipdabang.server.apiPayload.reponse.ResponseDto;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.apiPayload.exception.handler.RecipeException;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.service.RecipeService;
import zipdabang.server.validation.annotation.CheckPage;
import zipdabang.server.validation.annotation.CheckTempMember;
import zipdabang.server.validation.annotation.ExistRecipeCategory;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "레시피 관련 API", description = "레시피 관련 API 모음입니다.")
public class
RecipeRestController {

    private final RecipeService recipeService;

    @Operation(summary = "🍹figma 레시피 작성하기1, 레시피 등록 API 🔑 ✔", description = "레시피 (작성)등록 화면 API입니다. 임시저장 api는 별도로 있음. step이랑 ingredient 몇개 들어오는지 각Count에 적어주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4100", description = "레시피 작성시 누락된 내용이 있습니다. 미완료는 임시저장으로 가세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> createRecipe(
            @RequestPart(value = "content") RecipeRequestDto.CreateRecipeDto request,
            @RequestPart(value = "thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "stepImages") List<MultipartFile> stepImages,
            @CheckTempMember @AuthMember Member member) throws IOException {

        log.info("사용자가 준 정보 : {}", request.toString());

        Recipe recipe = recipeService.create(request, thumbnail, stepImages, member);
        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 수정 API 🔑 ✔", description = "레시피 수정 화면 API입니다. 내용 누락 불가능합니다. step이랑 ingredient 몇개 들어오는지 각Count에 적어주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4106", description = "BAD_REQUEST, 본인이 작성한 레시피가 아닙니다. 수정할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PatchMapping(value = "/members/recipes/{recipeId}")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> updateRecipe(
            @PathVariable(name = "recipeId") Long recipeId,
            @RequestPart(value = "content") RecipeRequestDto.UpdateRecipeDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "stepImages", required = false) List<MultipartFile> stepImages,
            @CheckTempMember @AuthMember Member member) throws IOException {

        log.info("사용자가 준 정보 : {}", request.toString());

        Recipe recipe = recipeService.update(recipeId, request, thumbnail, stepImages, member);
        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 임시저장, 레시피 임시저장 등록 API 🔑 ✔", description = "레시피 임시저장 화면 API입니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4111", description = "BAD_REQUEST, 해당 임시저장 Id가 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = {"/members/recipes/temp/{tempId}", "/members/recipes/temp"})
    public ResponseDto<RecipeResponseDto.TempRecipeStatusDto> createTempRecipe(
            @PathVariable(required = false) Long tempId,
            @RequestPart(value = "content") RecipeRequestDto.TempRecipeDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "stepImages", required = false) List<MultipartFile> stepImages,
            @CheckTempMember @AuthMember Member member) throws IOException {

        log.info("사용자가 준 정보 : {}", request.toString());

        TempRecipe tempRecipe;

        if (tempId==null)
            tempRecipe = recipeService.tempCreate(request, thumbnail, stepImages, member);
        else
            tempRecipe = recipeService.tempUpdate(tempId, request, thumbnail, stepImages, member);

        return ResponseDto.of(RecipeConverter.toTempRecipeStatusDto(tempRecipe));
    }

    @Operation(summary = "임시저장 레시피 가져오기 API 🔑 ✔", description = "임시저장 레시피 가져오기 화면 API입니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4111", description = "BAD_REQUEST, 해당 임시저장 Id가 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/temp/{tempId}")
    public ResponseDto<RecipeResponseDto.TempRecipeInfoDto> getTempRecipe(@PathVariable Long tempId, @AuthMember Member member) {

        TempRecipe tempRecipe = recipeService.getTempRecipe(tempId);

        return ResponseDto.of(RecipeConverter.toTempRecipeInfoDto(tempRecipe,  member));
    }


    @Operation(summary = "임시저장 레시피 삭제 API 🔑 ✔", description = "임시저장 레시피 삭제 API입니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4111", description = "BAD_REQUEST, 해당 임시저장 Id가 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @DeleteMapping(value = "/members/recipes/temp/{tempId}")
    public ResponseDto<String> deleteTempRecipe(
            @PathVariable Long tempId,
            @CheckTempMember @AuthMember Member member) {
        Boolean recipeDeleteBoolean = recipeService.deleteTempRecipe(tempId, member);

        if (recipeDeleteBoolean)
            return ResponseDto.of(tempId + " 임시저장 레시피 삭제 완료");
        else
            throw new RecipeException(CommonStatus.INTERNAL_ERROR);
    }

    @Operation(summary = "레시피 임시저장 ➡ 최종 저장 API 🔑 ✔", description = "\"레시피 임시저장 ➡ 최종 저장 API입니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4111", description = "BAD_REQUEST, 해당 임시저장 Id가 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/temp/{tempId}/save")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> tempRecipeToRecipe(
            @PathVariable Long tempId,
            @RequestBody RecipeRequestDto.RecipeCategoryList categoryList,
            @CheckTempMember @AuthMember Member member) {

        Recipe recipe = recipeService.createFromTempRecipe(tempId, categoryList, member);
        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "임시저장 레시피 목록 가져오기 API 🔑 ✔", description = "임시저장 레시피 목록 가져오기 화면 API입니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "2102", description = "OK, 목록이 없을 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, 0 이런거 주면 에러 뱉음"),
    })
    @GetMapping(value = "/members/recipes/temp")
    public ResponseDto<RecipeResponseDto.TempRecipePaging> getTempRecipeList(@CheckPage @RequestParam(name = "pageIndex") Integer pageIndex,
                                                                           @AuthMember Member member) {

        if (pageIndex == null)
            pageIndex = 1;

        pageIndex -= 1;

        Page<TempRecipe> tempRecipes = recipeService.getTempRecipeList(pageIndex, member);

        log.info(tempRecipes.toString());

        if (tempRecipes.getTotalElements() == 0)
            throw new RecipeException(RecipeStatus.TEMP_RECIPE_NOT_FOUND);
        if (pageIndex >= tempRecipes.getTotalPages())
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toTempRecipePaging(tempRecipes));
    }

    @Operation(summary = "🍹figma 레시피 상세페이지, 레시피 상세 정보 조회 API 🔑 ✔", description = "레시피 조회 화면 API입니다. 댓글은 처음 10개만 가져오고 나머지는 댓글 page api 드림")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4102", description = "BAD_REQUEST, 차단한 사용자의 recipe 입니다. 접근할 수 없습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
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

        return ResponseDto.of(RecipeConverter.toRecipeInfoDto(recipe, isOwner, isLiked, isScrapped, member));
    }

    @Operation(summary = "🍹figma 나의 레시피 삭제_알럿, 레시피 삭제 API 🔑 ✔", description = "레시피 삭제 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 삭제처리 되었습니다."),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4106", description = "BAD_REQUEST, 본인이 작성한 레시피가 아닙니다. 삭제할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @DeleteMapping("/members/recipes/{recipeId}")
    public ResponseDto<String> deleteRecipe(@PathVariable(name = "recipeId") Long recipeId, @CheckTempMember @AuthMember Member member) {
        Boolean recipeDeleteBoolean = recipeService.deleteRecipe(recipeId, member);

        if (recipeDeleteBoolean)
            return ResponseDto.of(recipeId + " 레시피 삭제 완료");
        else
            throw new RecipeException(CommonStatus.INTERNAL_ERROR);
    }

    @Operation(summary = "🍹figma 레시피2, 레시피 검색 카테고리 별 preview 화면 API 🔑 ✔", description = "검색한 레시피 카테고리별 조회 화면 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "keyword", description = "query string 검색할 단어")
    })
    @GetMapping(value = "/members/recipes/search/preview")
    public ResponseDto<RecipeResponseDto.SearchRecipePreviewListDto> searchRecipePreview(@RequestParam(name = "keyword", required = false) String keyword, @AuthMember Member member) {


        List<List<Recipe>> recipeLists = recipeService.searchRecipePreview(keyword, member);

        log.info(recipeLists.toString());

        return ResponseDto.of(RecipeConverter.toSearchRecipePreviewListDto(recipeLists, member));
    }

    @Operation(summary = "🍹figma 레시피2, 레시피 검색 목록조회 화면 API 🔑 ✔", description = "검색한 레시피 조회 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054", description = "BAD_REQUEST, 페이지 번호 0 이하입니다. 1 이상으로 주세요.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST, 페이지 인덱스 범위 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4105", description = "BAD_REQUEST, 해당 id를 가진 레시피 카테고리가 없습니다. 잘못 보내줬어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, 0 이런거 주면 에러 뱉음"),
            @Parameter(name = "keyword", description = "query string 검색할 단어")
    })
    @GetMapping(value = "/members/recipes/search/{categoryId}")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> searchRecipe(@ExistRecipeCategory @PathVariable Long categoryId, @RequestParam(name = "keyword") String keyword, @RequestParam(name = "pageIndex") Integer pageIndex, @AuthMember Member member) {

        if (pageIndex == null)
            pageIndex = 1;
        else if (pageIndex < 1)
            throw new RecipeException(CommonStatus.UNDER_PAGE_INDEX_ERROR);

        pageIndex -= 1;

        Page<Recipe> recipes = recipeService.searchRecipe(categoryId, keyword, pageIndex, member);

        if (recipes.getTotalElements() == 0)
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);
        if (pageIndex >= recipes.getTotalPages())
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피2, 레시피 카테고리 별 top5 화면 API 🔑 ✔", description = "레시피 카테고리별 top5 조회 화면 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4105", description = "BAD_REQUEST, 해당 id를 가진 레시피 카테고리가 없습니다. 잘못 보내줬어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),

            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/categories/{categoryId}/top5")
    public ResponseDto<RecipeResponseDto.PerCategoryPreview> RecipeCategoryTop5(@ExistRecipeCategory @PathVariable Long categoryId, @AuthMember Member member) {

        List<Recipe> recipeList = recipeService.getTop5RecipePerCategory(categoryId);

        log.info(recipeList.toString());

        return ResponseDto.of(RecipeConverter.toPerCategoryPreview(categoryId, recipeList ,member));
    }

    @Operation(summary = "🍹figma 레시피2, 카테고리 별 레시피 목록 조회 API 🔑 ✔", description = "카테고리 별 레시피 목록 조회 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4053", description = "BAD_REQUEST, 넘겨받은 categoryId와 일치하는 카테고리 없음. 1~6 사이로 보내세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST, 페이지 인덱스 범위 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4104", description = "BAD_REQUEST, 조회 방식 타입이 잘못되었습니다. likes, views, lastest중 하나로 보내주세요.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4105", description = "BAD_REQUEST, 해당 id를 가진 레시피 카테고리가 없습니다. 잘못 보내줬어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, 0 이런거 주면 에러 뱉음"),
            @Parameter(name = "order", description = "query string 조회 방식. 인기순: likes, 팔로우순: follow, 최신순: latest로 넘겨주세요, 기본값 latest")
    })
    @GetMapping(value = "/members/recipes/categories/{categoryId}")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> recipeListByCategory(@ExistRecipeCategory @PathVariable Long categoryId, @RequestParam(name = "order", required = false) String order, @CheckPage @RequestParam(name = "pageIndex") Integer pageIndex, @AuthMember Member member) {

        if (pageIndex == null)
            pageIndex = 1;

        pageIndex -= 1;

        Page<Recipe> recipes = recipeService.recipeListByCategory(categoryId, pageIndex, member, order);


        log.info(recipes.toString());

        if (recipes.getTotalElements() == 0)
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);
        if (pageIndex >= recipes.getTotalPages())
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피1, 모든사람/인플루언서/우리들의 레시피 미리보기 API 🔑 ✔", description = "5개씩 미리보기로 가져오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4103", description = "BAD_REQUEST, 레시피 작성자 타입이 잘못되었습니다. official, barista, common중 하나로 보내주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "writtenby", description = "query string 누가 쓴 레시피 종류인지. 공식: official, 바리스타: barista, 우리들: common으로 넘겨주세요")
    })
    @GetMapping(value = "/members/recipes/types/preview")
    public ResponseDto<RecipeResponseDto.RecipeListDto> recipeListPreviewWrittenBy(@RequestParam(name = "writtenby") String writtenby, @AuthMember Member member) {
        List<Recipe> recipes = recipeService.getWrittenByRecipePreview(writtenby, member);

        log.info(recipes.toString());

        if (recipes.size() == 0)
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);

        return ResponseDto.of(RecipeConverter.toPreviewRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🍹figma 레시피2, 모든사람/인플루언서/우리들의 레시피 목록 API 🔑", description = "레시피 목록 화면 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4053", description = "BAD_REQUEST, 넘겨받은 categoryId와 일치하는 카테고리 없음. 1~6 사이로 보내세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST, 페이지 인덱스 범위 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "writtenby", description = "query string 누가 쓴 레시피 종류인지. 공식: official, 바리스타: barista, 우리들: common으로 넘겨주세요"),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, -1 이런거 주면 에러 뱉음"),
            @Parameter(name = "order", description = "query string 조회 방식. 인기순: likes, 조회순: name, 최신순: latest로 넘겨주세요")
    })
    @GetMapping(value = "/members/recipes/types")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> recipeListWrittenBy(@RequestParam(name = "writtenby") String writtenby, @RequestParam(name = "order") String order, @CheckPage @RequestParam(name = "pageIndex") Integer pageIndex, @AuthMember Member member) {
        return null;
    }

    @Operation(summary = "특정 유저의 레시피 미리보기 목록 API 🔑 ✔", description = "특정 유저의 레시피 미리기보기 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 해당 memberId를 가진 사용자가 없습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/owner/preview/{memberId}")
    public ResponseDto<RecipeResponseDto.RecipeListDto> recipeByOwnerPreview(@PathVariable Long memberId, @AuthMember Member member) {
        List<Recipe> recipes = recipeService.getRecipeByOwnerPreview(memberId);

        return ResponseDto.of(RecipeConverter.toPreviewRecipeDtoList(recipes, member));
    }

    @Operation(summary = "특정 유저의 레시피 목록 API 🔑 ✔", description = "특정 유저의 레시피 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 해당 memberId를 가진 사용자가 없습니다.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST, 페이지 인덱스 범위 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, -1 이런거 주면 에러 뱉음"),
    })
    @GetMapping(value = "/members/recipes/owner/{memberId}")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> recipeByOwner(@CheckPage @RequestParam(name = "pageIndex") Integer pageIndex,
                                                                      @PathVariable Long memberId,
                                                                      @AuthMember Member member) {
        if (pageIndex == null)
            pageIndex = 1;

        pageIndex -= 1;

        Page<Recipe> recipes = recipeService.getRecipeByOwner(pageIndex, memberId);

        log.info(recipes.toString());

        if (recipes.getTotalElements() == 0)
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);
        if (pageIndex >= recipes.getTotalPages())
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingRecipeDtoList(recipes, member));
    }

    @Operation(summary = "🏠figma 홈1, 주간 베스트 레시피 API 🔑 ✔", description = "이번 주 베스트 레시피 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2100", description = "OK, 목록이 없을 경우, result = null", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping(value = "/members/recipes/week-best")
    public ResponseDto<RecipeResponseDto.WeekBestDtoList> recipeWeekBest(@AuthMember Member member) {
        List<WeeklyBestRecipe> bestRecipes = recipeService.WeekBestRecipe();

        return ResponseDto.of(RecipeConverter.toWeekBestDtoList(bestRecipes, member));
    }

    @Operation(summary = "레시피 스크랩/취소 API 🔑 ✔", description = "레시피 스크랩/취소 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4109", description = "BAD_REQUEST, 본인의 레시피입니다. 좋아요/스크랩할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/{recipeId}/scrap")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> recipeScrapOrCancel(@PathVariable Long recipeId, @CheckTempMember @AuthMember Member member) {
        Recipe recipe = recipeService.updateScrapOnRecipe(recipeId, member);

        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 좋아요/취소 API 🔑 ✔", description = "레시피 좋아요/취소 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4109", description = "BAD_REQUEST, 본인의 레시피입니다. 좋아요/스크랩할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/{recipeId}/likes")
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> recipeLikeOrCancel(@PathVariable Long recipeId, @CheckTempMember @AuthMember Member member) {

        Recipe recipe = recipeService.updateLikeOnRecipe(recipeId, member);

        return ResponseDto.of(RecipeConverter.toRecipeStatusDto(recipe));
    }

    @Operation(summary = "레시피 배너 이미지 API 🔑 ✔", description = "레시피 화면의 배너 이미지를 가져옵니다. order는 배너 순서를 의미합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/recipes/banners")
    public ResponseDto<RecipeResponseDto.RecipeBannerImageDto> showBanners() {
        List<RecipeBanner> recipeBannerList = recipeService.getRecipeBannerList();
        return ResponseDto.of(RecipeConverter.toRecipeBannerImageDto(recipeBannerList));
    }

    @Operation(summary = "레시피 카테고리 조회 API 🔑 ✔️", description = "레시피 카테고리 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @GetMapping("/members/recipes/categories")
    public ResponseDto<RecipeResponseDto.RecipeCategoryListDto> showCategoryList() {
        List<RecipeCategory> allCategories = recipeService.getAllRecipeCategories();
        return ResponseDto.of(RecipeConverter.RecipeCategoryListDto(allCategories));
    }

    @Operation(summary = "레시피 신고 API 🔑 ✔", description = "레시피 신고 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 레시피가 신고 되었습니다."),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4060", description = "BAD_REQUEST, 해당 id를 가진 신고 목록이 없습니다. 잘못 보내줬어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4109", description = "BAD_REQUEST, 본인의 레시피입니다. 신고/차단할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping("/members/recipes/{recipeId}/report/{reportId}")
    public ResponseDto<String> reportRecipe(@PathVariable Long recipeId, @PathVariable Long reportId, @CheckTempMember @AuthMember Member member) {
        Long reportedRecipeId = recipeService.reportRecipe(recipeId, reportId, member);

        return ResponseDto.of(reportedRecipeId+"번 레시피가 신고되었습니다.");
    }

    @Operation(summary = "댓글 등록 API 🔑 ✔", description = "레시피 (작성)등록 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PostMapping(value = "/members/recipes/{recipeId}/comments")
    public ResponseDto<RecipeResponseDto.CommentDto> createComment(@RequestBody RecipeRequestDto.createCommentDto request, @PathVariable Long recipeId, @CheckTempMember @AuthMember Member member) {
        Comment createdComment = recipeService.createComment(request.getComment(), recipeId, member);

        return ResponseDto.of(RecipeConverter.toCommentDto(createdComment, member));
    }

    @Operation(summary = "댓글 목록 화면 API 🔑 ✔", description = "댓글 API입니다. pageIndex로 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 목록이 있을 땐 이 응답임"),
            @ApiResponse(responseCode = "2101", description = "OK, 조회된 댓글 목록이 없을 경우", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054", description = "BAD_REQUEST, 페이지 번호 0 이하입니다. 1 이상으로 주세요.", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQUEST, 페이지 인덱스 범위 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "pageIndex", description = "query string 페이지 번호, 무조건 값 줘야 함, 0 이런거 주면 에러 뱉음"),
    })
    @GetMapping(value = "/members/recipes/{recipeId}/comments")
    public ResponseDto<RecipeResponseDto.CommentPageListDto> searchRecipe(@PathVariable Long recipeId, @RequestParam(name = "pageIndex") Integer pageIndex, @AuthMember Member member) {

        if (pageIndex == null)
            pageIndex = 1;
        else if (pageIndex < 1)
            throw new RecipeException(CommonStatus.UNDER_PAGE_INDEX_ERROR);

        pageIndex -= 1;

        Page<Comment> comments = recipeService.commentList(pageIndex, recipeId, member);


        log.info(comments.toString());

        if (pageIndex >= comments.getTotalPages())
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return ResponseDto.of(RecipeConverter.toPagingCommentDtoList(comments, member));
    }

    @Operation(summary = "댓글 삭제 API 🔑 ✔", description = "댓글 삭제 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 삭제처리 되었습니다."),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4107", description = "BAD_REQUEST, 해당 commentId를 가진 댓글이 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4108", description = "BAD_REQUEST, 본인이 작성한 댓글이 아닙니다. 삭제할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4112", description = "BAD_REQUEST, 해당 댓글은 넘겨준 레시피 Id에 존재하지 않습니다. 레시피 Id를 올바르게 보내주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @DeleteMapping("/members/recipes/{recipeId}/{commentId}")
    public ResponseDto<String> deleteComment(@PathVariable(name = "recipeId") Long recipeId, @PathVariable(name = "commentId") Long commentId, @CheckTempMember @AuthMember Member member) {
        Boolean commentDeleteBoolean = recipeService.deleteComment(recipeId,commentId, member);

        if (commentDeleteBoolean)
            return ResponseDto.of(commentId + " 댓글 삭제 완료");
        else
            throw new RecipeException(CommonStatus.INTERNAL_ERROR);
    }

    @Operation(summary = "댓글 수정 API 🔑 ✔", description = "댓글 수정 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 댓글이 수정 되었습니다."),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4107", description = "BAD_REQUEST, 해당 commentId를 가진 댓글이 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4108", description = "BAD_REQUEST, 본인이 작성한 댓글이 아닙니다. 수정할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4112", description = "BAD_REQUEST, 해당 댓글은 넘겨준 레시피 Id에 존재하지 않습니다. 레시피 Id를 올바르게 보내주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @PatchMapping("/members/recipes/{recipeId}/{commentId}")
    public ResponseDto<RecipeResponseDto.CommentDto> updateComment(@RequestBody RecipeRequestDto.updateCommentDto request, @PathVariable(name = "recipeId") Long recipeId, @PathVariable(name = "commentId") Long commentId, @CheckTempMember @AuthMember Member member) {
        Comment updateComment = recipeService.updateComment(request, recipeId,commentId, member);

        return ResponseDto.of(RecipeConverter.toCommentDto(updateComment, member));
    }

    @Operation(summary = "댓글 신고 API 🔑 ✔", description = "댓글 신고 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 댓글이 신고 되었습니다."),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4060", description = "BAD_REQUEST, 해당 id를 가진 신고 목록이 없습니다. 잘못 보내줬어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "BAD_REQUEST, 해당 recipeId를 가진 recipe가 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4107", description = "BAD_REQUEST, 해당 commentId를 가진 댓글이 없어요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4110", description = "BAD_REQUEST, 본인의 댓글입니다. 신고/차단할 수 없습니다", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4112", description = "BAD_REQUEST, 해당 댓글은 넘겨준 레시피 Id에 존재하지 않습니다. 레시피 Id를 올바르게 보내주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
    })
    @GetMapping("/members/recipes/{recipeId}/{commentId}/report/{reportId}")
    public ResponseDto<String> reportComment(@PathVariable Long recipeId, @PathVariable Long commentId,@PathVariable Long reportId, @CheckTempMember @AuthMember Member member) {
        Long reportedCommentId = recipeService.reportComment(recipeId,commentId, reportId, member);

        return ResponseDto.of(reportedCommentId+"번 댓글이 신고되었습니다.");
    }

    @Operation(summary = "내가 좋아요 누른 레시피 목록 조회 API 🔑 ✔", description = "내가 좋아요 누른 레시피 목록 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 좋아요 누른 레시피 조회 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054", description = "BAD_REQEUST , 페이지 번호가 없거나 0 이하", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQEUST , 페이지 번호가 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "page", description = "페이지 번호, 1부터 시작")
    })
    @GetMapping("/members/likeRecipes")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> memberLikeRecipeList(@CheckTempMember @AuthMember Member member, @RequestParam(name = "page", required = true) @CheckPage Integer page) {
        if (page == null)
            page = 1;
        else if (page < 1)
            throw new MemberException(CommonStatus.UNDER_PAGE_INDEX_ERROR);
        page -= 1;

        return ResponseDto.of(recipeService.getLikeRecipes(page, member));
    }


    @Operation(summary = "내가 스크랩 누른 레시피 목록 조회 API 🔑 ✔", description = "내가 스크랩 누른 레시피 목록 조회 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "2000", description = "OK, 스크랩 누른 레시피 조회 성공"),
            @ApiResponse(responseCode = "4003", description = "UNAUTHORIZED, 토큰 모양이 이상함, 토큰 제대로 주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4005", description = "UNAUTHORIZED, 엑세스 토큰 만료, 리프레시 토큰 사용", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4008", description = "UNAUTHORIZED, 토큰 없음, 토큰 줘요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4052", description = "BAD_REQUEST, 사용자가 없습니다. 이 api에서 이거 생기면 백앤드 개발자 호출", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4054", description = "BAD_REQEUST , 페이지 번호가 없거나 0 이하", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "4055", description = "BAD_REQEUST , 페이지 번호가 초과함", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "5000", description = "SERVER ERROR, 백앤드 개발자에게 알려주세요", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "member", hidden = true),
            @Parameter(name = "page", description = "페이지 번호, 1부터 시작")
    })
    @GetMapping("/members/scrapRecipes")
    public ResponseDto<RecipeResponseDto.RecipePageListDto> memberScrapRecipeList(@CheckTempMember @AuthMember Member member, @RequestParam(name = "page", required = true) @CheckPage Integer page) {
        if (page == null)
            page = 1;
        else if (page < 1)
            throw new MemberException(CommonStatus.UNDER_PAGE_INDEX_ERROR);
        page -= 1;

        return ResponseDto.of(recipeService.getScrapRecipes(page, member));
    }
}
