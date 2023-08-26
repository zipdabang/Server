package zipdabang.server.service;

import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.List;

public interface RecipeService {

    Recipe create(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member)throws IOException;

    Recipe getRecipe(Long recipeId, Member member);

    Boolean getLike(Recipe recipe, Member member);

    Boolean getScrap(Recipe recipe, Member member);

    Boolean checkOwner(Recipe recipe, Member member);
}
