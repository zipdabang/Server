package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.BlockedComment;

public interface BlockedRecipeRepository extends JpaRepository<BlockedComment,Long> {
}
