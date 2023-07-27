package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Recipes;

public interface RecipeRepository extends JpaRepository<Recipes, Long> {
}
