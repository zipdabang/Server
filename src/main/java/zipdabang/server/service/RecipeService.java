package zipdabang.server.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeBanner;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.List;

public interface RecipeService {

    List<RecipeBanner> getRecipeBannerList();

    Recipe create(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member)throws IOException;

    Recipe getRecipe(Long recipeId, Member member);

    Boolean getLike(Recipe recipe, Member member);

    Boolean getScrap(Recipe recipe, Member member);

    Boolean checkOwner(Recipe recipe, Member member);

    Page<Recipe> searchRecipe(Long categoryId, String keyword, Integer pageIndex, Member member);

    List<Recipe> getWrittenByRecipePreview(String writtenby, Member member);

     Recipe updateLikeOnRecipe(Long recipeId, Member member);

    Recipe updateScrapOnRecipe(Long recipeId, Member member);

    Page<Recipe> recipeListByCategory(Long categoryId, Integer pageIndex, Member member, String order);

    List<RecipeCategory> getAllRecipeCategories();

    List<List<Recipe>> searchRecipePreview(String keyword, Member member);


    boolean checkRecipeCategoryExist(Long categoryId);
}
