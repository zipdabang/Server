package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.market.member.Member;
import zipdabang.server.domain.recipe.Ingredient;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategoryMapping;
import zipdabang.server.domain.recipe.Step;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.recipeRepositories.RecipeRepository;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    private static RecipeRepository staticRecipeRepository;
    private static CategoryRepository staticCategoryRepository;

    @PostConstruct
    public void init() {
        this.staticRecipeRepository = this.recipeRepository;
        this.staticCategoryRepository = this.categoryRepository;
    }

    public static Recipe toReicepe(RecipeRequestDto.CreateRecipeDto request,Member member){

        Recipe recipe = Recipe.builder()
                .isInfluencer(member.isInfluencer())
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .member(member)
                .build();

        request.getCategoryId().stream()
                .map(categoryId -> toRecipeCategoryMappingDto(categoryId, recipe))
                .collect(Collectors.toList())
                .forEach(categoryMapping -> recipe.addCategory(categoryMapping));

        request.getIngredients().stream()
                .map(ingredient -> toIngredientDto(ingredient, recipe))
                .collect(Collectors.toList())
                .forEach(ingredient -> recipe.addIngredient(ingredient));

        request.getSteps().stream()
                .map(step-> toStepDto(step, recipe))
                .collect(Collectors.toList())
                .forEach(step -> recipe.addStep(step));

        /*사진 업로드
        MultipartFile thumbnail = request.getThumbnailUrl();

        String imageUrl = null;
        if(thumbnail != null)
            imageUrl = uploadThumbnail(thumbnail,recipe);
        recipe.setThumbnail(imageUrl);
*/
        return recipe;
    }

    private static String uploadThumbnail(MultipartFile thumbnail, Recipe recipe) throws IOException {
        return null;
    }

    private static RecipeCategoryMapping toRecipeCategoryMappingDto(Long categoryId, Recipe recipe) {
        return RecipeCategoryMapping.builder()
                .category(staticCategoryRepository.findById(categoryId).get())
                .recipe(recipe)
                .build();
    }

    //step도 multipart 고려해야함
    private static Step toStepDto(RecipeRequestDto.StepDto step, Recipe recipe) {
        return Step.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build();
    }

    private static Ingredient toIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, Recipe recipe) {
        return Ingredient.builder()
                .name(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .recipe(recipe)
                .build();
    }
}
