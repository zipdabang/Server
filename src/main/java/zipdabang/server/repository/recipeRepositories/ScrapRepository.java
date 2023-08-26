package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.market.member.Member;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.Scrap;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByRecipeAndMember(Recipe recipe, Member member);

}
