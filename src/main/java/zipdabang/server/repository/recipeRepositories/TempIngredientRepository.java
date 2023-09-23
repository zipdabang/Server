package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.TempIngredient;
import zipdabang.server.domain.recipe.TempRecipe;

import java.util.List;
import java.util.Optional;

public interface TempIngredientRepository extends JpaRepository<TempIngredient, Long> {

    void deleteAllByTempRecipe(TempRecipe tempRecipe);
}
