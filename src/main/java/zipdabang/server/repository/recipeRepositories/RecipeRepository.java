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
    Page<Recipe> findByNameContainingAndMemberNotIn(String keyword, List<Member> members, Pageable pageable);
    Page<Recipe> findByNameContaining(String keyword, Pageable pageable);

    List<Recipe> findTop5ByMemberNotInOrderByCreatedAtDesc(List<Member> members);
    List<Recipe> findTop5ByIsInfluencerTrueAndMemberNotInOrderByCreatedAtDesc(List<Member> members);
    List<Recipe> findTop5ByIsInfluencerFalseAndMemberNotInOrderByCreatedAtDesc(List<Member> members);

    List<Recipe> findTop5ByOrderByCreatedAtDesc();

    List<Recipe> findTop5ByIsInfluencerTrueOrderByCreatedAtDesc();

    List<Recipe> findTop5ByIsInfluencerFalseOrderByCreatedAtDesc();

    Page<Recipe> findByIdIn(List<Long> recipeIdList, PageRequest createdAt);

    Page<Recipe> findByIdInAndMemberNotIn(List<Long> recipeIdList, List<Member> blockedMember, PageRequest createdAt);
}
