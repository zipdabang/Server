package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategory;

import java.util.List;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory,Long> {

    List<RecipeCategory> findAllById(Long categoryId);
}
