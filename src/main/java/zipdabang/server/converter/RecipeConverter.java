package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.RetryableStatusCodeException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
//import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.RecipeException;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.utils.converter.TimeConverter;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final RecipeRepository recipeRepository;
    private final TempStepRepository tempStepRepository;
    private final RecipeCategoryMappingRepository recipeCategoryMappingRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;
//    private final CategoryRepository categoryRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final RecipeBannerRepository recipeBannerRepository;
    private final CommentRepository commentRepository;
    private final AmazonS3Manager amazonS3Manager;
    private final TimeConverter timeConverter;

    private static RecipeRepository staticRecipeRepository;
    private static TempStepRepository staticTempStepRepository;
    private static RecipeCategoryMappingRepository staticRecipeCategoryMappingRepository;


    private static LikesRepository staticLikesRepository;
    private static ScrapRepository staticScrapRepository;

//    private static CategoryRepository staticCategoryRepository;
    private static RecipeCategoryRepository staticRecipeCategoryRepository;
    private static RecipeBannerRepository staticRecipeBannerRepository;
    private static CommentRepository staticCommentRepository;
    private static AmazonS3Manager staticAmazonS3Manager;
    private static TimeConverter staticTimeConverter;

    public static RecipeResponseDto.PerCategoryPreview toPerCategoryPreview(Long categoryId, List<Recipe> recipeList, Member member) {
        return RecipeResponseDto.PerCategoryPreview.builder()
                .categoryId(categoryId)
                .totalElements(recipeList.size())
                .recipeList(recipeList.stream()
                        .map(recipe -> toRecipePreviewDto(recipe,member))
                        .collect(Collectors.toList()))
                .build();
    }

    public static RecipeResponseDto.RecipePreviewDto toRecipePreviewDto(Recipe recipe, Member member) {
        return RecipeResponseDto.RecipePreviewDto.builder()
                .recipeId(recipe.getId())
                .recipeName(recipe.getName())
                .nickname(recipe.getMember().getNickname())
                .likes(recipe.getTotalLike())
                .scraps(recipe.getTotalScrap())
                .isLiked(staticLikesRepository.findByRecipeAndMember(recipe, member).isPresent())
                .isScrapped(staticScrapRepository.findByRecipeAndMember(recipe,member).isPresent())
                .build();
    }


    @PostConstruct
    public void init() {
        this.staticRecipeRepository = this.recipeRepository;
        this.staticRecipeCategoryMappingRepository = this.recipeCategoryMappingRepository;
//        this.staticCategoryRepository = this.categoryRepository;
        this.staticRecipeCategoryRepository = this.recipeCategoryRepository;
        this.staticRecipeBannerRepository = this.recipeBannerRepository;
        this.staticAmazonS3Manager = this.amazonS3Manager;
        this.staticLikesRepository = this.likesRepository;
        this.staticScrapRepository = this.scrapRepository;
        this.staticTimeConverter = this.timeConverter;
        this.staticTempStepRepository = this.tempStepRepository;
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
                .likes(recipe.getTotalLike())
                .scraps(recipe.getTotalScrap())
                .isLiked(staticLikesRepository.findByRecipeAndMember(recipe, member).isPresent())
                .isScrapped(staticScrapRepository.findByRecipeAndMember(recipe,member).isPresent())
                .build();
    }

    public static List<Step> toStep(RecipeRequestDto.CreateRecipeDto request, Recipe recipe, List<MultipartFile> stepImages) {
        return request.getSteps().stream()
                .map(step-> {
                    if (step.getDescription() == null)
                        throw new RecipeException(Code.NULL_RECIPE_ERROR);
                    try {
                        return toStepDto(step, recipe, stepImages);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<TempStep> toTempStep(RecipeRequestDto.TempRecipeDto request, TempRecipe tempRecipe, List<MultipartFile> stepImages) {
        return request.getSteps().stream()
                .map(step-> {
                    try {
                        return toTempStepDto(step, tempRecipe, stepImages);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<RecipeCategoryMapping> toRecipeCategory(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
        return request.getCategoryId().stream()
                .map(recipeCategoryId -> toRecipeCategoryMappingDto(recipeCategoryId, recipe))
                .collect(Collectors.toList());
    }

    public static List<Ingredient> toIngredient(RecipeRequestDto.CreateRecipeDto request, Recipe recipe) {
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
                .comments(Long.valueOf(recipe.getCommentList().size()))
                .scraps(recipe.getTotalScrap())
                .isLiked(isLiked)
                .isScrapped(isScrapped)
                .build();
    }

    public static Recipe toRecipe(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, Member member) throws IOException {

        Recipe recipe = Recipe.builder()
                .isInfluencer(member.isInfluencer())
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
            throw new RecipeException(Code.NULL_RECIPE_ERROR);
        recipe.setThumbnail(imageUrl);

        return recipe;
    }

    public static TempRecipe toTempRecipe(RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, Member member) throws IOException {

        TempRecipe tempRecipe = TempRecipe.builder()
                .isInfluencer(member.isInfluencer())
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
                .category(staticRecipeCategoryRepository.findById(categoryId).get())
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
            throw new RecipeException(Code.NULL_RECIPE_ERROR);
        createdStep.setImage(imageUrl);

        return createdStep;
    }

    private static TempStep toTempStepDto(RecipeRequestDto.TempStepDto step, TempRecipe tempRecipe, List<MultipartFile> stepImages) throws IOException {
        TempStep createdTempStep = TempStep.builder()
                .imageUrl(step.getStepUrl())
                .stepNum(step.getStepNum())
                .description(step.getDescription())
                .tempRecipe(tempRecipe)
                .build();

        Optional<TempStep> findTempStep = staticTempStepRepository.findByTempRecipeAndStepNum(tempRecipe, step.getStepNum());

        if(step.getStepUrl() == null) {
            if(findTempStep.isPresent() && findTempStep.get().getImageUrl() != null) {
                staticAmazonS3Manager.deleteFile(RecipeConverter.toKeyName(findTempStep.get().getImageUrl()).substring(1));
            }

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
            createdTempStep.setImage(step.getStepUrl());
        }

        staticTempStepRepository.delete(findTempStep.get());

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

}
