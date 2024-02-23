package zipdabang.server.repository.recipeRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.RecipeCategory;

import java.util.List;
import java.util.Optional;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory,Long> {

    List<RecipeCategory> findAllById(Long categoryId);

    Optional<RecipeCategory> findById(Long id);
}
