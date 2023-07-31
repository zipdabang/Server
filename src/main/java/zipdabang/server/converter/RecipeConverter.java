package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.repository.recipeRepositories.RecipeRepository;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final RecipeRepository recipeRepository;

    private static RecipeRepository staticRecipeRepository;

    @PostConstruct
    public void init() {
        this.staticRecipeRepository = this.recipeRepository;
    }

    public static void toReicepe(RecipeRequestDto.CreateRecipeDto request){

    }
}
