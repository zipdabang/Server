package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
