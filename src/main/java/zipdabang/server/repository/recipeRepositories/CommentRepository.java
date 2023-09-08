package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.Recipe;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByMemberNotIn(List<Member> blockedMember, PageRequest createdAt);

    Page<Comment> findByIdNotIn(List<Long> blockedComment, PageRequest createdAt);

    Page<Comment> findByIdNotInAndMemberNotIn(List<Long> blockedComment, List<Member> blockedMember, PageRequest createdAt);
}
