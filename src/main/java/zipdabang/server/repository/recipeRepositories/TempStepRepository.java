package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.TempRecipe;
import zipdabang.server.domain.recipe.TempStep;

import java.util.List;
import java.util.Optional;

public interface TempStepRepository extends JpaRepository<TempStep, Long> {
    Optional<TempStep> findByTempRecipeAndStepNum(TempRecipe tempRecipe, Integer stepNum);

    void deleteAllByTempRecipe(TempRecipe tempRecipe);

    List<TempStep> findAllByTempRecipe(TempRecipe tempRecipe);
}
