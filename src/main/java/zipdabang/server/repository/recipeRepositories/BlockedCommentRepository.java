package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.BlockedComment;
import zipdabang.server.domain.recipe.Comment;

import java.util.List;
import java.util.Optional;

public interface BlockedCommentRepository extends JpaRepository<BlockedComment,Long> {
    List<BlockedComment> findByOwner(Member member);

    Boolean existsByOwnerAndBlocked(Member member, Comment findComment);
}
