package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findTop5ByIdInAndNameContainingOrderByCreatedAtDesc(List<Long> recipeIdList, String keyword);

    List<Recipe> findTop5ByIdInAndNameContainingAndMemberNotInOrderByCreatedAtDesc(List<Long> recipeIdList, String keyword, List<Member> blockedMember);
}
