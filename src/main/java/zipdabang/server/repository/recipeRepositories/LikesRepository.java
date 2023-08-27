package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.domain.recipe.Recipe;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByRecipeAndMember(Recipe recipe, Member member);
}
