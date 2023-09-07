package zipdabang.server.repository.recipeRepositories;


import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Step;

import java.util.List;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findAllByRecipeId(Long recipeId);
}
