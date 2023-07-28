package zipdabang.server.web.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.auth.handler.annotation.AuthMember;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.domain.Member;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "레시피 관련 API", description = "레시피 관련 API 모음입니다.")
public class RecipeController {

    @Parameters({
            @Parameter(name = "member", hidden = true)
    })
    @PostMapping(value = "/members/recipes",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> createRecipe(@ModelAttribute RecipeRequestDto.CreateRecipeDto request, @AuthMember Member member){
        return null;
    }
}
