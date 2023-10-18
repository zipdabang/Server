package zipdabang.server.repository.recipeRepositories.recipeRepositoryImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.QComment;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.CommentRepositoryCustom;

import java.util.List;
import java.util.stream.Collectors;

import static zipdabang.server.domain.recipe.QComment.comment;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final BlockedMemberRepository blockedMemberRepository;

    QComment qComment = comment;

    @Override
    public List<Comment> getCommentList(Integer pageIndex, Integer pageSize, Member member, Recipe recipe) {
        return queryFactory
                .selectFrom(comment)
                .where(blockedMemberNotInForComment(member),
                        comment.recipe.eq(recipe))
                .orderBy(comment.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long commentListTotalCount(Member member, Recipe recipe) {
        return queryFactory
                .select(comment.count())
                .from(comment)
                .where(blockedMemberNotInForComment(member),
                        comment.recipe.eq(recipe)
                )
                .fetchOne();
    }

    private BooleanExpression blockedMemberNotInForComment(Member member) {
        List<Member> blockedMember = getBlockedMember(member);

        return blockedMember.isEmpty() ? null : comment.member.notIn(blockedMember);
    }

    private List<Member> getBlockedMember(Member member) {
        List<Member> blockedMember = blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());
        return blockedMember;
    }
}
