package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.domain.recipe.RecipeCategoryMapping;

import java.util.List;
import java.util.Optional;

public interface RecipeCategoryMappingRepository extends JpaRepository<RecipeCategoryMapping, Long> {
    List<RecipeCategoryMapping> findByCategoryIn(List<RecipeCategory> recipeCategories);
}
