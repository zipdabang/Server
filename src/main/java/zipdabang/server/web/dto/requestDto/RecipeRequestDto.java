package zipdabang.server.web.dto.requestDto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RecipeRequestDto {

    @Getter @Setter
    public static class CreateRecipeDto{

        Long categoryId;
        Boolean isInfluencer;
        String name;
        MultipartFile thumbnailUrl;
        List<StepDto> steps;
        List<NewIngredientDto> ingredients;
    }

    public static class NewIngredientDto{
        private String IngredientName;
        private String quantity;
        private MultipartFile image;
    }

    public static class StepDto{
        private Integer stepNum;
        private String stepTitle;
        private String description;
        private MultipartFile image;
    }
}
