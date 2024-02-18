package zipdabang.server.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import java.io.IOException;
import java.util.List;

public interface RecipeService {

    List<RecipeBanner> getRecipeBannerList();

    Recipe create(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member)throws IOException;

    Recipe getRecipe(Long recipeId, Member member);

    Boolean getLike(Recipe recipe, Member member);

    Boolean getScrap(Recipe recipe, Member member);

    Boolean checkOwner(Recipe recipe, Member member);

    Page<Recipe> searchRecipe(Long categoryId, String keyword, String order, Integer pageIndex, Member member);

    List<Recipe> getWrittenByRecipePreview(String writtenby, Member member);

     Recipe updateLikeOnRecipe(Long recipeId, Member member);

    Recipe updateScrapOnRecipe(Long recipeId, Member member);

    Page<Recipe> recipeListByCategory(Long categoryId, Integer pageIndex, Member member, String order);

    List<RecipeCategory> getAllRecipeCategories();

    List<List<Recipe>> searchRecipePreview(String keyword, Member member);


    boolean checkRecipeCategoryExist(Long categoryId);

    Boolean deleteRecipe(Long recipeId, Member member);

    Comment createComment(String content, Long recipeId, Member member);

    Page<Comment> commentList(Integer pageIndex, Long recipeId, Member member);

    Boolean deleteComment(Long recipeId, Long commentId, Member member);

    Comment updateComment(RecipeRequestDto.updateCommentDto request, Long recipeId, Long commentId, Member member);

    Long reportComment(Long recipeId, Long commentId, Long reportId, Member member);

    Long reportRecipe(Long recipeId, Long reportId, Member member);

    TempRecipe tempCreate(RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException;

    TempRecipe tempUpdate(Long tempId, RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException;

    List<Recipe> getTop5RecipePerCategory(Long categoryId);

    List<Recipe> getRecipeByOwnerPreview(Long memberId);

    Page<Recipe> getRecipeByOwner(Integer pageIndex, Long memberId);

    List<WeeklyBestRecipe> WeekBestRecipe();

    Boolean deleteTempRecipe(Long tempId, Member member);

    Recipe createFromTempRecipe(Long tempId, RecipeRequestDto.RecipeCategoryList categoryList, Member member);

    TempRecipe getTempRecipe(Long tempId);
    RecipeResponseDto.RecipePageListDto getLikeRecipes(Integer page, Member member);
    RecipeResponseDto.RecipePageListDto getScrapRecipes(Integer page, Member member);


    Page<TempRecipe> getTempRecipeList(Integer pageIndex, Member member);

    Recipe update(Long recipeId, RecipeRequestDto.UpdateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException;

    List<Recipe> getMyRecipePreview(Member member);

    Page<Recipe> getMyRecipeList(Integer pageIndex, Member member);

    Page<Recipe> getWrittenByRecipe(Integer pageIndex, String writtenby, String order, Member member);

    Boolean checkOwnerBlocked(Recipe recipe, Member member);

    Boolean checkIsLiked(Recipe recipe, Member member);

    Boolean checkIsScrapped(Recipe recipe, Member member);

    RecipeCategory getRecipeCategory(Long categoryId);

    Long searchRecipeCounting(Long categoryId, String keyword, Member member);

    Long getrecipeListByCategoryCounting(Long categoryId, Member member);

    Long getWrittenByRecipeCounting(String writtenby, Member member);

    Long getCommentCount(Recipe recipe, Member member);

    TestRecipe testCreate(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages) throws IOException;

    TestRecipe getTestRecipe(Long recipeId);

    Page<TestRecipe> testRecipeListByCategory(Long categoryId, Integer pageIndex, String order);

    Boolean deleteTestRecipe();

    TestRecipe testCreateWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request);
}
