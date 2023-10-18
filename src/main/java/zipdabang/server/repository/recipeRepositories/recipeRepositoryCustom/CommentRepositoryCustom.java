package zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom;

import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.Recipe;

import java.util.List;

public interface CommentRepositoryCustom {

    List<Comment> getCommentList(Integer pageIndex, Integer pageSize, Member member, Recipe recipe);

    Long commentListTotalCount(Member member, Recipe recipe);
}
