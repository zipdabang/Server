package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.ReportedRecipe;

public interface ReportedRecipeRepository extends JpaRepository<ReportedRecipe, Long> {
}
