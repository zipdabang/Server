package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.RecipeException;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.domain.test.TestIngredient;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.domain.test.TestRecipeCategoryMapping;
import zipdabang.server.domain.test.TestStep;
import zipdabang.server.service.RecipeService;
import zipdabang.server.utils.converter.TimeConverter;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final AmazonS3Manager amazonS3Manager;
    private final TimeConverter timeConverter;
    private final RecipeService recipeService;


    private static AmazonS3Manager staticAmazonS3Manager;
    private static TimeConverter staticTimeConverter;
    private static RecipeService staticRecipeService;


    @PostConstruct
    public void init() {
        this.staticAmazonS3Manager = this.amazonS3Manager;
        this.staticTimeConverter = this.timeConverter;
        this.staticRecipeService = this.recipeService;
    }

    public static String toKeyName(String imageUrl) {
        String input = imageUrl;

        Pattern regex = Pattern.compile(staticAmazonS3Manager.getPattern());
        Matcher matcher = regex.matcher(input);
        String extractedString = null;
        if (matcher.find())
            extractedString = matcher.group(1);

        return extractedString;

    }

    private static final ExecutorService ioExecutor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 8));

    public static RecipeResponseDto.RecipeStatusDto toTestRecipeStatusDto(TestRecipe recipe) {
        return RecipeResponseDto.RecipeStatusDto.builder()
                .recipeId(recipe.getId())
                .calledAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .build();
    }

    public static String uploadTestThumbnail(MultipartFile thumbnail) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUUID();
        String keyName = staticAmazonS3Manager.generateTestThumbnailKeyName(uuid);
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, thumbnail);
        log.info("S3에 업로드 한 test thumbnail 파일의 url : {}", fileUrl);
        return fileUrl;
    }

    public static String uploadTestStep(MultipartFile stepImage) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUUID();
        String keyName = staticAmazonS3Manager.generateTestStepKeyName(uuid);
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, stepImage);
        log.info("S3에 업로드 한 test step 파일의 url : {}", fileUrl);
        return fileUrl;
    }

    public static TestRecipe toTestRecipe(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail) throws IOException {

        CompletableFuture<TestRecipe> buildRecipe = new CompletableFuture<>();
        CompletableFuture<String> setThumbnail = new CompletableFuture<>();

        buildRecipe.complete(TestRecipe.builder()
                .isBarista(false)
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .build());

        if(thumbnail != null)
            setThumbnail.complete(uploadTestThumbnail(thumbnail));
        else
            throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);

        return buildRecipe.thenCombine(setThumbnail, (recipe, imageUrl) -> {
            recipe.setThumbnail(imageUrl);
            return recipe;
        }).join();
    }

    public static TestRecipe toTestRecipeWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request){

        return TestRecipe.builder()
                .isBarista(false)
                .name(request.getName())
                .intro(request.getIntro())
                .thumbnailUrl(request.getThumbnailUrl())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .build();
    }


    public static CompletableFuture<List<TestRecipeCategoryMapping>> toTestRecipeCategory(List<Long> categoryIds, TestRecipe recipe) {
        return CompletableFuture.completedFuture(categoryIds.stream().parallel()
                .map(recipeCategoryId -> toTestRecipeCategoryMappingDto(recipeCategoryId, recipe))
                .collect(Collectors.toList()));
    }

    private static TestRecipeCategoryMapping toTestRecipeCategoryMappingDto(Long categoryId, TestRecipe recipe) {
        return TestRecipeCategoryMapping.builder()
                .category(staticRecipeService.getRecipeCategory(categoryId))
                .recipe(recipe)
                .build();
    }

    public static CompletableFuture<List<TestStep>> toTestStep(RecipeRequestDto.CreateRecipeDto request, TestRecipe recipe, List<MultipartFile> stepImages) {
        return CompletableFuture.supplyAsync(() -> request.getSteps().stream().parallel()
                .map(step-> {
                    if (step.getDescription() == null)
                        throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
                    try {
                        return toTestStepDto(step, recipe, stepImages);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()), ioExecutor
        );
    }

    public static CompletableFuture<List<TestStep>> toTestStepWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request, TestRecipe recipe) {
        return CompletableFuture.supplyAsync(() -> request.getSteps().stream().parallel()
                .map(step-> {
                    if (step.getDescription() == null)
                        throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
                    try {
                        return toTestStepWithImageUrlDto(step, recipe);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()), ioExecutor
        );
    }

    private static TestStep toTestStepDto(RecipeRequestDto.StepDto step, TestRecipe recipe, List<MultipartFile> stepImages) throws IOException {

        CompletableFuture<TestStep> buildStep = new CompletableFuture<>();
        CompletableFuture<String> setStep = new CompletableFuture<>();

        buildStep.complete(TestStep.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build());

        for (int i = 0; i <= stepImages.size(); i++) {
            Integer imageNum = Integer.parseInt(stepImages.get(i).getOriginalFilename().substring(0,1)) + 1;
            if (imageNum == step.getStepNum()){
                MultipartFile stepImage = stepImages.get(i);
                setStep.complete(uploadTestStep(stepImage));
                break;
            }
            else if(i == stepImages.size())
                throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
        }

        return buildStep.thenCombine(setStep, (createStep, imageUrl) -> {
            createStep.setImage(imageUrl);
            return createStep;
        }).join();

    }

    private static TestStep toTestStepWithImageUrlDto(RecipeRequestDto.StepWithImageUrlDto step, TestRecipe recipe) throws IOException {

        return TestStep.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .imageUrl(step.getStepUrl())
                .recipe(recipe)
                .build();
    }

    public static CompletableFuture<List<TestIngredient>> toTestIngredient(RecipeRequestDto.CreateRecipeDto request, TestRecipe recipe) {
        return CompletableFuture.completedFuture(request.getIngredients().stream().parallel()
                .map(ingredient -> toTestIngredientDto(ingredient, recipe))
                .collect(Collectors.toList()));
    }

    public static CompletableFuture<List<TestIngredient>> toTestIngredientWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request, TestRecipe recipe) {
        return CompletableFuture.completedFuture(request.getIngredients().stream().parallel()
                .map(ingredient -> toTestIngredientDto(ingredient, recipe))
                .collect(Collectors.toList()));
    }

    private static TestIngredient toTestIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, TestRecipe recipe) {

        return TestIngredient.builder()
                .name(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .recipe(recipe)
                .build();
    }

    public static RecipeResponseDto.RecipeInfoDto toTestRecipeInfoDto(TestRecipe recipe) {
        return RecipeResponseDto.RecipeInfoDto.builder()
                .recipeInfo(toResponseTestRecipeDto(recipe))
                .ownerId(0L)
                .isOwner(false)
                .steps(toResponseTestStepDto(recipe))
                .ingredients(toResponseTestIngredientDto(recipe))
                .build();
    }

    public static RecipeResponseDto.RecipeDto toResponseTestRecipeDto(TestRecipe recipe){

        return  RecipeResponseDto.RecipeDto.builder()
                .recipeId(recipe.getId())
                .categoryId(getTestCategoryIds(recipe))
                .recipeName(recipe.getName())
                .ownerImage("")
                .nickname("test")
                .thumbnailUrl(recipe.getThumbnailUrl()+"?size=thumbnail")
                .time(recipe.getTime())
                .intro(recipe.getIntro())
                .recipeTip(recipe.getRecipeTip())
                .createdAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(recipe.getUpdatedAt()))
                .likes(recipe.getTotalLike())
                .comments(0L)
                .scraps(recipe.getTotalScrap())
                .isLiked(false)
                .isScrapped(false)
                .build();
    }

    public static List<Long> getTestCategoryIds(TestRecipe recipe){
        return recipe.getCategoryMappingList().stream()
                .map(categoryMapping -> categoryMapping.getCategory().getId())
                .collect(Collectors.toList());
    }

    public static List<RecipeResponseDto.StepDto> toResponseTestStepDto(TestRecipe recipe) {

        return recipe.getStepList().stream()
                .map(step-> RecipeResponseDto.StepDto.builder()
                        .stepNum(step.getStepNum())
                        .description(step.getDescription())
                        .image(step.getImageUrl()+"?size=step")
                        .build())
                .collect(Collectors.toList());
    }

    public static List<RecipeResponseDto.IngredientDto> toResponseTestIngredientDto(TestRecipe recipe) {
        return recipe.getIngredientList().stream()
                .map(ingredient -> RecipeResponseDto.IngredientDto.builder()
                        .IngredientName(ingredient.getName())
                        .quantity(ingredient.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    public static RecipeResponseDto.RecipePageListDto toPagingTestRecipeDtoList(Page<TestRecipe> recipes) {
        return RecipeResponseDto.RecipePageListDto.builder()
                .recipeList(recipes.toList().stream()
                        .map(recipe -> toResponseTestRecipeSimpleDto(recipe))
                        .collect(Collectors.toList()))
                .totalElements(recipes.getTotalElements())
                .currentPageElements(recipes.getNumberOfElements())
                .totalPage(recipes.getTotalPages())
                .isFirst(recipes.isFirst())
                .isLast(recipes.isLast())
                .build();
    }

    private static RecipeResponseDto.RecipeSimpleDto toResponseTestRecipeSimpleDto(TestRecipe recipe) {
        return RecipeResponseDto.RecipeSimpleDto.builder()
                .recipeId(recipe.getId())
                .categoryId(getTestCategoryIds(recipe))
                .recipeName(recipe.getName())
                .nickname("test")
                .thumbnailUrl(recipe.getThumbnailUrl()+"?size=preview")
                .createdAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(recipe.getUpdatedAt()))
                .comments(0L)
                .likes(recipe.getTotalLike())
                .scraps(recipe.getTotalScrap())
                .isLiked(false)
                .isScrapped(false)
                .build();
    }

}
