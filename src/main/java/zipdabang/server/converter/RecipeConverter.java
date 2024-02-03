package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.apiPayload.exception.handler.RecipeException;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

    public static RecipeResponseDto.PerCategoryPreview toPerCategoryPreview(Long categoryId, List<Recipe> recipeList, Member member) {

        AtomicInteger index = new AtomicInteger(1);

        return RecipeResponseDto.PerCategoryPreview.builder()
                .categoryId(categoryId)
                .totalElements(recipeList.size())
                .recipeList(recipeList.stream()
                        .map(recipe -> toRecipePreviewDto(recipe,member, index.getAndIncrement()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static RecipeResponseDto.RecipePreviewDto toRecipePreviewDto(Recipe recipe, Member member, Integer rank) {
        return RecipeResponseDto.RecipePreviewDto.builder()
                .recipeId(recipe.getId())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .recipeName(recipe.getName())
                .nickname(recipe.getMember().getNickname())
                .likes(recipe.getTotalLike())
                .comments(staticRecipeService.getCommentCount(recipe,member))
                .isLiked(staticRecipeService.checkIsLiked(recipe, member))
                .isScrapped(staticRecipeService.checkIsScrapped(recipe,member))
                .isBlocked(staticRecipeService.checkOwnerBlocked(recipe,member))
                .ownerId(recipe.getMember().getMemberId())
                .rank(rank)
                .build();
    }

    public static Recipe toRecipeFromTemp(TempRecipe tempRecipe, List<TempStep> tempSteps, List<TempIngredient> tempIngredients, Member member) {
        return Recipe.builder()
                .isBarista(member.isBarista())
                .isOfficial(false)
                .name(tempRecipe.getName())
                .thumbnailUrl(tempRecipe.getThumbnailUrl())
                .intro(tempRecipe.getIntro())
                .recipeTip(tempRecipe.getRecipeTip())
                .time(tempRecipe.getTime())
                .member(member)
                .build();
    }

    public static List<Step> toStepFromTemp(List<TempStep> tempSteps, Recipe recipe) {
        return tempSteps.stream()
                .map(tempStep -> toStepDtoFromTemp(tempStep, recipe))
                .collect(Collectors.toList());
    }

    private static Step toStepDtoFromTemp(TempStep tempStep, Recipe recipe) {
        return Step.builder()
                .stepNum(tempStep.getStepNum())
                .imageUrl(tempStep.getImageUrl())
                .description(tempStep.getDescription())
                .recipe(recipe)
                .build();
    }

    public static List<Ingredient> toIngredientFromTemp(List<TempIngredient> tempIngredients, Recipe recipe) {
        return tempIngredients.stream()
                .map(tempIngredient -> toIngredientDtoFromTemp(tempIngredient, recipe))
                .collect(Collectors.toList());
    }

    private static Ingredient toIngredientDtoFromTemp(TempIngredient tempIngredient, Recipe recipe) {
        return Ingredient.builder()
                .name(tempIngredient.getName())
                .quantity(tempIngredient.getQuantity())
                .recipe(recipe)
                .build();
    }


    public static RecipeResponseDto.TempRecipePaging toTempRecipePaging(Page<TempRecipe> tempRecipes) {
        return RecipeResponseDto.TempRecipePaging.builder()
                .TempRecipeList(tempRecipes.stream()
                        .map(tempRecipe -> toTempRecipeSimpleDto(tempRecipe))
                        .collect(Collectors.toList()))
                .totalElements(tempRecipes.getTotalElements())
                .currentPageElements(tempRecipes.getNumberOfElements())
                .totalPage(tempRecipes.getTotalPages())
                .isFirst(tempRecipes.isFirst())
                .isLast(tempRecipes.isLast())
                .build();
    }

    private static RecipeResponseDto.TempRecipeSimpleDto toTempRecipeSimpleDto(TempRecipe tempRecipe) {
        return RecipeResponseDto.TempRecipeSimpleDto.builder()
                .tempId(tempRecipe.getId())
                .thumbnailUrl(tempRecipe.getThumbnailUrl())
                .recipeName(tempRecipe.getName())
                .updatedAt(TimeConverter.ConvertTime(tempRecipe.getUpdatedAt()))
                .build();
    }

    public static RecipeResponseDto.RecipePageListDto toPagingRecipeDtoList(Page<Recipe> recipes, Member member) {
        return RecipeResponseDto.RecipePageListDto.builder()
                .recipeList(recipes.toList().stream()
                        .map(recipe -> toResponseRecipeSimpleDto(recipe, member))
                        .collect(Collectors.toList()))
                .totalElements(recipes.getTotalElements())
                .currentPageElements(recipes.getNumberOfElements())
                .totalPage(recipes.getTotalPages())
                .isFirst(recipes.isFirst())
                .isLast(recipes.isLast())
                .build();
    }

    public static RecipeResponseDto.WeekBestDtoList toWeekBestDtoList(List<WeeklyBestRecipe> bestRecipes, Member member) {
        return RecipeResponseDto.WeekBestDtoList.builder()
                .recipeList(bestRecipes.stream()
                        .map(bestRecipe -> toResponseWeekBestDto(bestRecipe.getRecipe(), member, bestRecipe.getRanking()))
                        .collect(Collectors.toList()))
                .totalElements(bestRecipes.size())
                .build();
    }

    private static RecipeResponseDto.RecipeSimpleDtoBest toResponseWeekBestDto(Recipe recipe, Member member, Integer rank) {
        return RecipeResponseDto.RecipeSimpleDtoBest.builder()
                .rank(rank)
                .recipeId(recipe.getId())
                .recipeName(recipe.getName())
                .nickname(recipe.getMember().getNickname())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .createdAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(recipe.getUpdatedAt()))
                .likes(recipe.getTotalLike())
                .comments(staticRecipeService.getCommentCount(recipe,member))
                .scraps(recipe.getTotalScrap())
                .isLiked(staticRecipeService.checkIsLiked(recipe,member))
                .isScrapped(staticRecipeService.checkIsScrapped(recipe,member))
                .isBlocked(staticRecipeService.checkOwnerBlocked(recipe,member))
                .ownerId(recipe.getMember().getMemberId())
                .build();
    }

    public static RecipeResponseDto.RecipeListDto toPreviewRecipeDtoList(List<Recipe> recipes, Member member) {
        return RecipeResponseDto.RecipeListDto.builder()
                .recipeList(recipes.stream()
                        .map(recipe -> toResponseRecipeSimpleDto(recipe,member))
                        .collect(Collectors.toList()))
                .totalElements(recipes.size())
                .build();
    }

    public static RecipeResponseDto.SearchRecipePreviewListDto toSearchRecipePreviewListDto(List<List<Recipe>> recipeLists, Member member) {
        AtomicLong index = new AtomicLong(1);

        return RecipeResponseDto.SearchRecipePreviewListDto.builder()
                .recipeList(recipeLists.stream()
                        .map(recipeList -> toSearchRecipePreviewByCategoryDto(index.getAndIncrement(), recipeList, member))
                        .collect(Collectors.toList()))
                .build();
    }

    private static RecipeResponseDto.SearchRecipePreviewByCategoryDto toSearchRecipePreviewByCategoryDto(Long index, List<Recipe> recipeList, Member member) {
        return RecipeResponseDto.SearchRecipePreviewByCategoryDto.builder()
                .recipeList(recipeList.stream()
                        .map(recipe -> toResponseRecipeSimpleDto(recipe, member))
                        .collect(Collectors.toList()))
                .categoryId(index)
                .elements(recipeList.size())
                .build();
    }

    private static RecipeResponseDto.RecipeSimpleDto toResponseRecipeSimpleDto(Recipe recipe, Member member) {
        return RecipeResponseDto.RecipeSimpleDto.builder()
                .recipeId(recipe.getId())
                .categoryId(getCategoryIds(recipe))
                .recipeName(recipe.getName())
                .nickname(recipe.getMember().getNickname())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .createdAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(recipe.getUpdatedAt()))
                .comments(staticRecipeService.getCommentCount(recipe,member))
                .likes(recipe.getTotalLike())
                .scraps(recipe.getTotalScrap())
                .isLiked(staticRecipeService.checkIsLiked(recipe,member))
                .isScrapped(staticRecipeService.checkIsScrapped(recipe,member))
                .build();
    }

    public static List<Step> toStep(RecipeRequestDto.CreateRecipeDto request, Recipe recipe, List<MultipartFile> stepImages) {
        return request.getSteps().stream()
                .map(step-> {
                    if (step.getDescription() == null)
                        throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
                    try {
                        return toStepDto(step, recipe, stepImages);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<TempStep> toTempStep(RecipeRequestDto.TempRecipeDto request, TempRecipe tempRecipe, List<MultipartFile> stepImages, List<String> presentImageUrls) {
        return request.getSteps().stream()
                .map(step-> {
                    try {
                        return toTempStepDto(step, tempRecipe, stepImages, presentImageUrls);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<Step> toUpdateStep(RecipeRequestDto.UpdateRecipeDto request, Recipe recipe, List<MultipartFile> stepImages, List<String> presentImageUrls) {
        return request.getSteps().stream()
                .map(step-> {
                    try {
                        return toUpdateStepDto(step, recipe, stepImages, presentImageUrls);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private static Step toUpdateStepDto(RecipeRequestDto.UpdateStepDto step, Recipe recipe, List<MultipartFile> stepImages, List<String> presentImageUrls) throws IOException {
        Step updatedStep = Step.builder()
                .imageUrl(step.getStepUrl())
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build();


        if(step.getStepUrl() == null) {

            if (stepImages != null) {
                MultipartFile stepImage = null;

                for (int i = 0; i < stepImages.size(); i++) {
                    Integer imageNum = Integer.parseInt(stepImages.get(i).getOriginalFilename().substring(0, 1)) + 1;
                    if (imageNum == step.getStepNum()) {
                        stepImage = stepImages.get(i);
                        break;
                    }
                }

                String imageUrl = null;
                if (stepImage != null) {
                    imageUrl = uploadStepImage(stepImage);
                    updatedStep.setImage(imageUrl);
                }
            }
        }
        else{
            presentImageUrls.remove(step.getStepUrl());
        }

        return updatedStep;
    }

    public static RecipeResponseDto.TempRecipeInfoDto toTempRecipeInfoDto(TempRecipe tempRecipe, Member member) {
        return RecipeResponseDto.TempRecipeInfoDto.builder()
                .recipeInfo(toTempRecipeInfo(tempRecipe))
                .steps(tempRecipe.getStepList().stream()
                        .map(tempStep -> toTempStepInfo(tempStep))
                        .collect(Collectors.toList()))
                .ingredients(tempRecipe.getIngredientList().stream()
                        .map(tempIngredient -> toTempIngredientInfo(tempIngredient))
                        .collect(Collectors.toList()))
                .build();
    }

    private static RecipeResponseDto.TempIngredientDto toTempIngredientInfo(TempIngredient tempIngredient) {
        return RecipeResponseDto.TempIngredientDto.builder()
                .IngredientName(tempIngredient.getName())
                .quantity(tempIngredient.getQuantity())
                .build();
    }

    private static RecipeResponseDto.TempStepDto toTempStepInfo(TempStep tempStep) {
        return RecipeResponseDto.TempStepDto.builder()
                .stepNum(tempStep.getStepNum())
                .description(tempStep.getDescription())
                .image(tempStep.getImageUrl())
                .build();
    }

    private static RecipeResponseDto.TempRecipeDto toTempRecipeInfo(TempRecipe tempRecipe) {
        return RecipeResponseDto.TempRecipeDto.builder()
                .recipeName(tempRecipe.getName())
                .thumbnailUrl(tempRecipe.getThumbnailUrl())
                .time(tempRecipe.getTime())
                .intro(tempRecipe.getIntro())
                .recipeTip(tempRecipe.getRecipeTip())
                .updatedAt(TimeConverter.ConvertTime(tempRecipe.getUpdatedAt()))
                .build();
    }

    public static List<RecipeCategoryMapping> toRecipeCategory(List<Long> categoryIds, Recipe recipe) {
        return categoryIds.stream()
                .map(recipeCategoryId -> toRecipeCategoryMappingDto(recipeCategoryId, recipe))
                .collect(Collectors.toList());
    }

    public static List<Ingredient> toIngredient(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
        return request.getIngredients().stream()
                .map(ingredient -> toIngredientDto(ingredient, recipe))
                .collect(Collectors.toList());
    }

    public static List<Ingredient> toUpdateIngredient(RecipeRequestDto.UpdateRecipeDto request, Recipe recipe) {
        return request.getIngredients().stream()
                .map(ingredient -> toIngredientDto(ingredient, recipe))
                .collect(Collectors.toList());
    }

    public static List<TempIngredient> toTempIngredient(RecipeRequestDto.TempRecipeDto request, TempRecipe tempRecipe) {
        return request.getIngredients().stream()
                .map(ingredient -> toTempIngredientDto(ingredient, tempRecipe))
                .collect(Collectors.toList());
    }

    public static RecipeResponseDto.RecipeStatusDto toRecipeStatusDto(Recipe recipe) {
        return RecipeResponseDto.RecipeStatusDto.builder()
                .recipeId(recipe.getId())
                .calledAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .build();
    }

    public static RecipeResponseDto.TempRecipeStatusDto toTempRecipeStatusDto(TempRecipe tempRecipe) {
        return RecipeResponseDto.TempRecipeStatusDto.builder()
                .tempId(tempRecipe.getId())
                .calledAt(staticTimeConverter.ConvertTime(tempRecipe.getCreatedAt()))
                .build();
    }


    public static RecipeResponseDto.RecipeInfoDto toRecipeInfoDto(Recipe recipe, Boolean isOwner, Boolean isLiked, Boolean isScrapped, Member member) {
        return RecipeResponseDto.RecipeInfoDto.builder()
                .recipeInfo(toResponseRecipeDto(recipe, isLiked, isScrapped, member))
                .ownerId(recipe.getMember().getMemberId())
                .isOwner(isOwner)
                .steps(toResponseStepDto(recipe))
                .ingredients(toResponseIngredientDto(recipe))
                .build();
    }

    public static List<RecipeResponseDto.StepDto> toResponseStepDto(Recipe recipe) {

        return recipe.getStepList().stream()
                .map(step-> RecipeResponseDto.StepDto.builder()
                        .stepNum(step.getStepNum())
                        .description(step.getDescription())
                        .image(step.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<RecipeResponseDto.IngredientDto> toResponseIngredientDto(Recipe recipe) {
        return recipe.getIngredientList().stream()
                .map(ingredient -> RecipeResponseDto.IngredientDto.builder()
                        .IngredientName(ingredient.getName())
                        .quantity(ingredient.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<Long> getCategoryIds(Recipe recipe){
         return recipe.getCategoryMappingList().stream()
                .map(categoryMapping -> categoryMapping.getCategory().getId())
                .collect(Collectors.toList());
    }

    public static RecipeResponseDto.RecipeDto toResponseRecipeDto(Recipe recipe, Boolean isLiked, Boolean isScrapped, Member member){

        return  RecipeResponseDto.RecipeDto.builder()
                .recipeId(recipe.getId())
                .categoryId(getCategoryIds(recipe))
                .recipeName(recipe.getName())
                .ownerImage(recipe.getMember().getProfileUrl())
                .nickname(recipe.getMember().getNickname())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .time(recipe.getTime())
                .intro(recipe.getIntro())
                .recipeTip(recipe.getRecipeTip())
                .createdAt(staticTimeConverter.ConvertTime(recipe.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(recipe.getUpdatedAt()))
                .likes(recipe.getTotalLike())
                .comments(staticRecipeService.getCommentCount(recipe,member))
                .scraps(recipe.getTotalScrap())
                .isLiked(isLiked)
                .isScrapped(isScrapped)
                .build();
    }

    public static Recipe toRecipe(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, Member member) throws IOException {

        Recipe recipe = Recipe.builder()
                .isBarista(member.isBarista())
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .member(member)
                .build();

        String imageUrl = null;
        if(thumbnail != null)
            imageUrl = uploadThumbnail(thumbnail);
        else
            throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
        recipe.setThumbnail(imageUrl);

        return recipe;
    }

    public static TempRecipe toTempRecipe(RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, Member member) throws IOException {

        TempRecipe tempRecipe = TempRecipe.builder()
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .member(member)
                .build();


        String imageUrl = null;
        if(thumbnail != null)
            imageUrl = uploadThumbnail(thumbnail);
        tempRecipe.setThumbnail(imageUrl);

        return tempRecipe;
    }

    public static String uploadThumbnail(MultipartFile thumbnail) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUUID();
        String keyName = staticAmazonS3Manager.generateRecipeKeyName(uuid);
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, thumbnail);
        log.info("S3에 업로드 한 recipe thumbnail 파일의 url : {}", fileUrl);
        return fileUrl;
    }

    private static RecipeCategoryMapping toRecipeCategoryMappingDto(Long categoryId, Recipe recipe) {
        return RecipeCategoryMapping.builder()
                .category(staticRecipeService.getRecipeCategory(categoryId))
                .recipe(recipe)
                .build();
    }

    private static Step toStepDto(RecipeRequestDto.StepDto step, Recipe recipe, List<MultipartFile> stepImages) throws IOException {
        Step createdStep = Step.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build();

        MultipartFile stepImage = null;

        for (int i = 0; i < stepImages.size(); i++) {
            Integer imageNum = Integer.parseInt(stepImages.get(i).getOriginalFilename().substring(0,1)) + 1;
            if (imageNum == step.getStepNum()){
                stepImage = stepImages.get(i);
                break;
            }
        }

        String imageUrl = null;
        if(stepImages != null)
            imageUrl = uploadStepImage(stepImage);
        else
            throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
        createdStep.setImage(imageUrl);

        return createdStep;
    }

    private static TempStep toTempStepDto(RecipeRequestDto.TempStepDto step, TempRecipe tempRecipe, List<MultipartFile> stepImages, List<String> presentImageUrls) throws IOException {
        TempStep createdTempStep = TempStep.builder()
                .imageUrl(step.getStepUrl())
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .tempRecipe(tempRecipe)
                .build();


        if(step.getStepUrl() == null) {

            if (stepImages != null) {
                MultipartFile stepImage = null;

                for (int i = 0; i < stepImages.size(); i++) {
                    Integer imageNum = Integer.parseInt(stepImages.get(i).getOriginalFilename().substring(0, 1)) + 1;
                    if (imageNum == step.getStepNum()) {
                        stepImage = stepImages.get(i);
                        break;
                    }
                }

                String imageUrl = null;
                if (stepImage != null) {
                    imageUrl = uploadStepImage(stepImage);
                    createdTempStep.setImage(imageUrl);
                }
            }
        }
        else{
            presentImageUrls.remove(step.getStepUrl());
        }

        return createdTempStep;
    }

    private static String uploadStepImage(MultipartFile stepImage) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUUID();
        String keyName = staticAmazonS3Manager.generateStepKeyName(uuid);
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, stepImage);
        log.info("S3에 업로드 한 recipe step 파일의 url : {}", fileUrl);
        return fileUrl;
    }


    private static Ingredient toIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, Recipe recipe) {
        return Ingredient.builder()
                .name(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .recipe(recipe)
                .build();
    }

    private static TempIngredient toTempIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, TempRecipe tempRecipe) {
        return TempIngredient.builder()
                .name(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .tempRecipe(tempRecipe)
                .build();
    }

    public static Likes toLikes(Recipe recipe, Member member) {
        return Likes.builder()
                .recipe(recipe)
                .member(member)
                .build();
    }

    public static Scrap toScrap(Recipe recipe, Member member) {
        return Scrap.builder()
                .recipe(recipe)
                .member(member)
                .build();
    }
    public static RecipeResponseDto.RecipeCategoryListDto RecipeCategoryListDto(List<RecipeCategory> categoryList) {
        List<RecipeResponseDto.RecipeCategoryDto> recipeCategoryDtoList = categoryList.stream()
                .map(category -> toRecipeCategoryDto(category)).collect(Collectors.toList());

        return RecipeResponseDto.RecipeCategoryListDto.builder()
                .beverageCategoryList(recipeCategoryDtoList)
                .size(recipeCategoryDtoList.size())
                .build();
    }
    public static RecipeResponseDto.RecipeCategoryDto toRecipeCategoryDto (RecipeCategory category){
        return RecipeResponseDto.RecipeCategoryDto.builder()
                .categoryName(category.getName())
                .imageUrl(category.getImageUrl())
                .id(category.getId())
                .build();
    }

    public static RecipeResponseDto.RecipeBannerImageDto toRecipeBannerImageDto(List<RecipeBanner> recipeBannerList) {
        return RecipeResponseDto.RecipeBannerImageDto.builder()
                .bannerList(toRecipeBannerDto(recipeBannerList))
                .size(recipeBannerList.size())
                .build();
    }

    private static List<RecipeResponseDto.RecipeBannerDto> toRecipeBannerDto(List<RecipeBanner> recipeBannerList) {
        return recipeBannerList.stream()
                .map(recipeBanner -> RecipeResponseDto.RecipeBannerDto.builder()
                        .order(recipeBanner.getInOrder())
                        .imageUrl(recipeBanner.getImageUrl())
                        .searchKeyword(recipeBanner.getSearchKeyword())
                        .build())
                .collect(Collectors.toList());
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

    public static Comment toComment(String content, Recipe findRecipe, Member member) {
        return Comment.builder()
                .content(content)
                .recipe(findRecipe)
                .member(member)
                .build();
    }

    public static RecipeResponseDto.CommentDto toCommentDto(Comment createdComment, Member member) {
        return RecipeResponseDto.CommentDto.builder()
                .content(createdComment.getContent())
                .ownerNickname(createdComment.getMember().getNickname())
                .ownerImage(createdComment.getMember().getProfileUrl())
                .isOwner(createdComment.getMember() == member)
                .createdAt(staticTimeConverter.ConvertTime(createdComment.getCreatedAt()))
                .updatedAt(staticTimeConverter.ConvertTime(createdComment.getUpdatedAt()))
                .ownerId(createdComment.getMember().getMemberId())
                .commentId(createdComment.getId())
                .build();
    }

    public static RecipeResponseDto.CommentPageListDto toPagingCommentDtoList(Page<Comment> comments, Member member) {
        return RecipeResponseDto.CommentPageListDto.builder()
                .CommentList(comments.stream()
                        .map(comment -> toCommentDto(comment,member))
                        .collect(Collectors.toList()))
                .totalElements(comments.getTotalElements())
                .currentPageElements(comments.getNumberOfElements())
                .totalPage(comments.getTotalPages())
                .isFirst(comments.isFirst())
                .isLast(comments.isLast())
                .build();
    }

    public static ReportedRecipe toRecipeReport(Report report, Recipe recipe, Member member) {
        return ReportedRecipe.builder()
                .reportId(report)
                .reported(recipe)
                .owner(member)
                .build();
    }

    public static ReportedComment toCommentReport(Report report, Comment comment, Member member) {
        return ReportedComment.builder()
                .reportId(report)
                .reported(comment)
                .owner(member)
                .build();
    }

    /**
     * 부하 테스트용
     */
//    @Value("${cloud.aws.s3.user-default-image}")
//    String userDefaultImage;

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

    public static String uploadTestStep(MultipartFile thumbnail) throws IOException {
        Uuid uuid = staticAmazonS3Manager.createUUID();
        String keyName = staticAmazonS3Manager.generateTestStepKeyName(uuid);
        String fileUrl = staticAmazonS3Manager.uploadFile(keyName, thumbnail);
        log.info("S3에 업로드 한 test step 파일의 url : {}", fileUrl);
        return fileUrl;
    }

    public static TestRecipe toTestRecipe(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail) throws IOException {

        TestRecipe recipe = TestRecipe.builder()
                .isBarista(false)
                .name(request.getName())
                .intro(request.getIntro())
                .recipeTip(request.getRecipeTip())
                .time(request.getTime())
                .build();

        String imageUrl = null;
        if(thumbnail != null)
            imageUrl = uploadTestThumbnail(thumbnail);
        else
            throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
        recipe.setThumbnail(imageUrl);

        return recipe;
    }

    public static List<TestRecipeCategoryMapping> toTestRecipeCategory(List<Long> categoryIds, TestRecipe recipe) {
        return categoryIds.stream()
                .map(recipeCategoryId -> toTestRecipeCategoryMappingDto(recipeCategoryId, recipe))
                .collect(Collectors.toList());
    }

    private static TestRecipeCategoryMapping toTestRecipeCategoryMappingDto(Long categoryId, TestRecipe recipe) {
        log.info("categoryMappingDto Thread: " + categoryId);
        return TestRecipeCategoryMapping.builder()
                .category(staticRecipeService.getRecipeCategory(categoryId))
                .recipe(recipe)
                .build();
    }

    public static List<TestStep> toTestStep(RecipeRequestDto.CreateRecipeDto request, TestRecipe recipe, List<MultipartFile> stepImages) {
        return request.getSteps().stream().parallel()
                .map(step-> {
                    if (step.getDescription() == null)
                        throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
                    try {
                        return toTestStepDto(step, recipe, stepImages);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private static TestStep toTestStepDto(RecipeRequestDto.StepDto step, TestRecipe recipe, List<MultipartFile> stepImages) throws IOException {
        log.info("stepDto Thread: " + step.getStepNum()+"(stepNum)");

        TestStep createdStep = TestStep.builder()
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .recipe(recipe)
                .build();

        MultipartFile stepImage = null;

        for (int i = 0; i < stepImages.size(); i++) {
            Integer imageNum = Integer.parseInt(stepImages.get(i).getOriginalFilename().substring(0,1)) + 1;
            if (imageNum == step.getStepNum()){
                stepImage = stepImages.get(i);
                break;
            }
        }

        String imageUrl = null;
        if(stepImages != null)
            imageUrl = uploadTestStep(stepImage);
        else
            throw new RecipeException(CommonStatus.NULL_RECIPE_ERROR);
        createdStep.setImage(imageUrl);

        return createdStep;
    }

    public static List<TestIngredient> toTestIngredient(RecipeRequestDto.CreateRecipeDto request, TestRecipe recipe) {
        return request.getIngredients().stream()
                .map(ingredient -> toTestIngredientDto(ingredient, recipe))
                .collect(Collectors.toList());
    }

    private static TestIngredient toTestIngredientDto(RecipeRequestDto.NewIngredientDto ingredient, TestRecipe recipe) {
        log.info("ingredientDto Thread: " + ingredient.getIngredientName());

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
                .thumbnailUrl(recipe.getThumbnailUrl())
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
                        .image(step.getImageUrl())
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
                .thumbnailUrl(recipe.getThumbnailUrl())
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
