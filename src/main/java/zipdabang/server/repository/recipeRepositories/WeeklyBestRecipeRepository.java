package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.WeeklyBestRecipe;

public interface WeeklyBestRecipeRepository extends JpaRepository<WeeklyBestRecipe, Long> {
    Boolean existsByRecipe(Recipe findRecipe);

    void deleteByRecipe(Recipe findRecipe);

}
