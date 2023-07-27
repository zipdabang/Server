package zipdabang.server.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
public class RecipeController {

    @PostMapping(value = "/users/recipes",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseDto<RecipeResponseDto.RecipeStatusDto> createRecipe(@ModelAttribute RecipeRequestDto.CreateRecipeDto request){
        return null;
    }
}
