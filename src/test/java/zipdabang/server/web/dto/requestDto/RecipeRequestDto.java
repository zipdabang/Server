package zipdabang.server.web.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RecipeRequestDto {

    @Getter
    @Setter
    public static class CreateRecipeDto{
        List<Long> categoryId;
        String name;
        String time;
        String intro;
        String recipeTip;
        Integer stepCount;
        Integer ingredientCount;
        List<StepDto> steps;
        List<NewIngredientDto> ingredients;
    }

    @Getter
    public static class StepDto{
        private Integer stepNum;
        private String description;
    }

    @Getter
    public static class NewIngredientDto{
        private String ingredientName;
        private String quantity;
    }

    @Getter @Setter
    public static class CreateRecipeWithImageUrlDto{
        List<Long> categoryId;
        String name;
        String time;
        String intro;
        String recipeTip;
        String thumbnailUrl;
        Integer stepCount;
        Integer ingredientCount;
        List<StepWithImageUrlDto> steps;
        List<NewIngredientDto> ingredients;
    }

    @Getter
    public static class StepWithImageUrlDto{
        private Integer stepNum;
        private String stepUrl;
        private String description;
    }
}
