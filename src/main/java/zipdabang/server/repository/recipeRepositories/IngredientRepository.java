package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Ingredient;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.TempIngredient;
import zipdabang.server.domain.recipe.TempRecipe;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    void deleteAllByRecipe(Recipe recipe);

}
