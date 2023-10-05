package zipdabang.server.web.dto.responseDto;


import lombok.*;
import java.util.List;

public class RecipeResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeStatusDto{
        private Long recipeId;
        private String calledAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TempRecipeStatusDto{
        private Long tempId;
        private String calledAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PerCategoryPreview{
        private List<RecipePreviewDto> recipeList;
        private Long categoryId;
        private Integer totalElements;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipePreviewDto {
        private Long recipeId;
        private String recipeName;
        private String nickname;
        private String thumbnailUrl;
        private Long likes;
        private Long scraps;
        private Boolean isLiked;
        private Boolean isScrapped;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeSimpleDto {
        private Long recipeId;
        List<Long> categoryId;
        private String recipeName;
        private String nickname;
        private String thumbnailUrl;
        private String createdAt;
        private String updatedAt;
        private Long likes;
        private Long comments;
        private Long scraps;
        private Boolean isLiked;
        private Boolean isScrapped;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeDto {
        private Long recipeId;
        List<Long> categoryId;
        private String recipeName;
        private String ownerImage;
        private String nickname;
        private String thumbnailUrl;
        private String time;
        private String intro;
        private String recipeTip;
        private String createdAt;
        private String updatedAt;
        private Long likes;
        private Long comments;
        private Long scraps;
        private Boolean isLiked;
        private Boolean isScrapped;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeListDto {
        private List<RecipeSimpleDto> recipeList;
        Integer totalElements;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipePageListDto {
        private List<RecipeSimpleDto> recipeList;
        Long totalElements;
        Integer currentPageElements;
        Integer totalPage;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SearchRecipePreviewListDto {
        private List<SearchRecipePreviewByCategoryDto> recipeList;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SearchRecipePreviewByCategoryDto {
        private List<RecipeSimpleDto> recipeList;
        Long categoryId;
        Integer elements;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeInfoDto {
        private RecipeDto recipeInfo;
        private boolean isOwner;
        private List<StepDto> steps;
        private List<IngredientDto> ingredients;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class IngredientDto{
        private String IngredientName;
        private String quantity;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StepDto{
        private Integer stepNum;
        private String description;
        private String image;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CommentDto{
        private String ownerNickname;
        private String ownerImage;
        private Boolean isOwner;
        private String content;
        private String createdAt;
        private String updatedAt;
        private Long ownerId;
        private Long commentId;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeBannerDto{
        private Integer order;
        private String imageUrl;
        private String searchKeyword;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeBannerImageDto {
        List<RecipeBannerDto> bannerList;
        Integer size;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeCategoryDto{
        private Long id;
        private String categoryName;
        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeCategoryListDto{
        List<RecipeCategoryDto> beverageCategoryList;
        Integer size;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CommentPageListDto {
        private List<CommentDto> CommentList;
        Long totalElements;
        Integer currentPageElements;
        Integer totalPage;
        Boolean isFirst;
        Boolean isLast;
    }
}
