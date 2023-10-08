package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.Scrap;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    @Query("select s.recipe from Scrap s where s.member = :owner and s.recipe.member not in ( select b.blocked from BlockedMember b where b.owner = :owner ) ")
    Page<Recipe> findRecipeByMember(@Param("owner") Member owner, PageRequest pageRequest);
    Optional<Scrap> findByRecipeAndMember(Recipe recipe, Member member);

}
