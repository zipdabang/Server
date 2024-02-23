package zipdabang.server.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.List;

public interface RecipeService {
    public RecipeCategory getRecipeCategory(Long categoryId);

    TestRecipe testCreate(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages) throws IOException;

    TestRecipe getTestRecipe(Long recipeId);

    Page<TestRecipe> testRecipeListByCategory(Long categoryId, Integer pageIndex, String order);

    Boolean deleteTestRecipe();

    TestRecipe testCreateWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request);
}
