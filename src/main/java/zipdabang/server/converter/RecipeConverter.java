package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
//import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.domain.market.member.Member;
import zipdabang.server.domain.recipe.Ingredient;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategoryMapping;
import zipdabang.server.domain.recipe.Step;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.recipeRepositories.RecipeRepository;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
//    private final AmazonS3Manager amazonS3Manager;

    private static RecipeRepository staticRecipeRepository;
    private static CategoryRepository staticCategoryRepository;
//    private static AmazonS3Manager staticAmazonS3Manager;

    public static List<Step> toStep(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
        return request.getSteps().stream()
                .map(step-> {
                    try {
                        return toStepDto(step, recipe);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<RecipeCategoryMapping> toCategory(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
        return request.getCategoryId().stream()
                .map(categoryId -> toRecipeCategoryMappingDto(categoryId, recipe))
                .collect(Collectors.toList());
    }

    public static List<Ingredient> toIngredient(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
        return request.getIngredients().stream()
                .map(ingredient -> toIngredientDto(ingredient, recipe))
                .collect(Collectors.toList());
    }

    public static RecipeResponseDto.RecipeStatusDto toRecipeStatusDto(Recipe recipe) {
        return RecipeResponseDto.RecipeStatusDto.builder()
                .recipeId(recipe.getId())
                .calledAt(recipe.getCreatedAt())
                .build();
    }


    public static RecipeResponseDto.RecipeInfoDto toRecipeInfoDto(Recipe recipe, Boolean isLiked, Boolean isScrapped) {
        return RecipeResponseDto.RecipeInfoDto.builder()
                .build();

    }

    @PostConstruct
    public void init() {
        this.staticRecipeRepository = this.recipeRepository;
        this.staticCategoryRepository = this.categoryRepository;
//        this.staticAmazonS3Manager = this.amazonS3Manager;
    }

    public static Recipe toReicepe(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, Member member) throws IOException {

        Recipe recipe = Recipe.builder()
                .isInfluencer(member.isInfluencer())
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .member(member)
                .build();

    /*
        MultipartFile thumbnail = request.getThumbnailUrl();

        String imageUrl = null;
        if(thumbnail != null)
            imageUrl = uploadThumbnail(thumbnail);
        recipe.setThumbnail(imageUrl);
*/
        return recipe;
    }

    /*
    private static String uploadThumbnail(MultipartFile thumbnail) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUuid();
        String keyName = staticAmazonS3Manager.generateRecipeKeyName(uuid, thumbnail.getOriginalFilename());
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, thumbnail);
        log.info("S3에 업로드 한 파일의 url : {}", fileUrl);
        return fileUrl;
    }
*/
    private static RecipeCategoryMapping toRecipeCategoryMappingDto(Long categoryId, Recipe recipe) {
        return RecipeCategoryMapping.builder()
                .category(staticCategoryRepository.findById(categoryId).get())
                .recipe(recipe)
                .build();
    }

    private static Step toStepDto(RecipeRequestDto.StepDto step, Recipe recipe) throws IOException {
        Step createdStep = Step.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build();
/*
        MultipartFile stepImage = step.getImage();

        String imageUrl = null;
        if(stepImage != null)
            imageUrl = uploadStepImage(stepImage);
        createdStep.setImage(imageUrl);
*/
        return createdStep;
    }

    /*
    private static String uploadStepImage(MultipartFile stepImage) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUuid();
        String keyName = staticAmazonS3Manager.generateStepKeyName(uuid, stepImage.getOriginalFilename());
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, stepImage);
        log.info("S3에 업로드 한 파일의 url : {}", fileUrl);
        return fileUrl;
    }
    */

    private static Ingredient toIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, Recipe recipe) {
        return Ingredient.builder()
                .name(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .recipe(recipe)
                .build();
    }
}
