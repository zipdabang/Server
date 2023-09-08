package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.BlockedComment;

import java.util.List;

public interface BlockedCommentRepository extends JpaRepository<BlockedComment,Long> {
    List<BlockedComment> findByOwner(Member member);
}
