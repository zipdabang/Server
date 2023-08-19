package zipdabang.server.web.dto.responseDto;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RecipeResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeStatusDto{
        private Long recipeId;
        private LocalDateTime calledAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeDto {
        private Long recipeId;
        List<Long> categoryId;
        private String recipeName;
        private String owner;
        private String thumbnailUrl;
        private LocalDate createdAt;
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
    public static class RecipeSearchDto {
        private List<RecipeDto> recipeList;
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
    public static class RecipeInfoDto {
        private RecipeDto recipeInfo;
        private List<StepDto> steps;
        private List<IngredientDto> ingredients;
        private List<CommentDto> comments;
    }

    public static class IngredientDto{
        private String IngredientName;
        private String quantity;
    }

    public static class StepDto{
        private Integer stepNum;
        private String description;
        private MultipartFile image;
    }

    public static class CommentDto{
        private String owner;
        private String content;
        private LocalDate createdAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateRecipeDto {
        private Long recipeId;
    }
}
