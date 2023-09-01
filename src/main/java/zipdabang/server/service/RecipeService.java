package zipdabang.server.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.List;

public interface RecipeService {

    Recipe create(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member)throws IOException;

    Recipe getRecipe(Long recipeId, Member member);

    Boolean getLike(Recipe recipe, Member member);

    Boolean getScrap(Recipe recipe, Member member);

    Boolean checkOwner(Recipe recipe, Member member);

    Page<Recipe> searchRecipe(String keyword, Integer pageIndex, Member member);

    List<Recipe> getWrittenByRecipePreview(String writtenby, Member member);

     Recipe updateLikeOnRecipe(Long recipeId, Member member);

    Recipe updateScrapOnRecipe(Long recipeId, Member member);

    Page<Recipe> recipeListByCategory(Long categoryId, Integer pageIndex, Member member);

    List<RecipeCategory> getAllRecipeCategories();
}
