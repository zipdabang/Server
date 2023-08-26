package zipdabang.server.web.dto.responseDto;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.base.ResponseDto;

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
    public static class RecipeSimpleDto {
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
    public static class RecipeDto {
        private Long recipeId;
        List<Long> categoryId;
        private String recipeName;
        private String owner;
        private String thumbnailUrl;
        private String time;
        private String intro;
        private String recipeTip;
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
    public static class RecipeListDto {
        private List<RecipeSimpleDto> recipeList;
        Long totalElements;
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
    public static class RecipeInfoDto {
        private RecipeDto recipeInfo;
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
        private MultipartFile image;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CommentDto{
        private String owner;
        private String content;
        private LocalDate createdAt;
    }

}
