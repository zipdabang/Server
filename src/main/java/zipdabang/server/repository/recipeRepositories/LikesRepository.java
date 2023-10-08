package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.domain.recipe.Recipe;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    @Query("select l.recipe from Likes l where l.member = :owner and l.recipe.member not in ( select b.blocked from BlockedMember b where b.owner = :owner ) ")
    Page<Recipe> findRecipeByMember(@Param("owner") Member owner, PageRequest pageRequest);

    Optional<Likes> findByRecipeAndMember(Recipe recipe, Member member);
}
