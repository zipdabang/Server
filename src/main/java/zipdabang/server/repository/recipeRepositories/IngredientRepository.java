package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
