package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.RecipeCategoryMapping;

public interface RecipeCategoryMappingRepository extends JpaRepository<RecipeCategoryMapping, Long> {
}
