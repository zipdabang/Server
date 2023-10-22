package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findTop5ByIdInAndNameContainingOrderByCreatedAtDesc(List<Long> recipeIdList, String keyword);

    List<Recipe> findTop5ByIdInAndNameContainingAndMemberNotInOrderByCreatedAtDesc(List<Long> recipeIdList, String keyword, List<Member> blockedMember);

    List<Recipe> findTop5ByMemberOrderByCreatedAtDesc(Member member);

    Page<Recipe> findByMember(Member member, PageRequest pageRequest);

    List<Recipe> findTop5ByOrderByWeekLikeDescWeekScrapDescTotalLikeDescTotalScrapDesc();

    @Modifying
    @Query("UPDATE Recipe r SET r.weekLike = 0, r.weekScrap = 0, r.weekView = 0 WHERE r = :recipe")
    void updateWeeklyData(Recipe recipe);
}
