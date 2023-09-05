package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.RecipeBanner;

public interface RecipeBannerRepository extends JpaRepository<RecipeBanner, Long> {
}
