package zipdabang.server.web.dto.requestDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RecipeRequestDto {

    @Getter @Setter
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
    public static class NewIngredientDto{
        private String ingredientName;
        private String quantity;
    }

    @Getter
    public static class StepDto{
        private Integer stepNum;
        private String description;
    }
}
