package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.TempRecipe;

public interface TempRecipeRepository extends JpaRepository<TempRecipe, Long> {
}
