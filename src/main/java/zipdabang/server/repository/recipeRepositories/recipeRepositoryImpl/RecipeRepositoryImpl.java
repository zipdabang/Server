package zipdabang.server.repository.recipeRepositories.recipeRepositoryImpl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.RecipeException;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.QFollow;
import zipdabang.server.domain.recipe.QRecipe;
import zipdabang.server.domain.recipe.QRecipeCategoryMapping;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.RecipeRepositoryCustom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static zipdabang.server.domain.member.QFollow.follow;
import static zipdabang.server.domain.recipe.QRecipe.recipe;
import static zipdabang.server.domain.recipe.QRecipeCategoryMapping.recipeCategoryMapping;

@Slf4j
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final BlockedMemberRepository blockedMemberRepository;

    QRecipe qRecipe = recipe;
    QRecipeCategoryMapping qRecipeCategoryMapping = recipeCategoryMapping;


    @Override
    public List<Recipe> getWrittenByPreview(String writtenby, Member member, Integer previewSize) {
        return queryFactory
                .selectFrom(recipe)
                .where(blockedMemberNotInForRecipe(member),
                        checkWrittenBy(writtenby)
                )
                .limit(previewSize)
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> getTop5RecipePerCategory(Long categoryId) {
        return queryFactory
                .selectFrom(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping).fetchJoin()
                .where(
                        recipeCategoryMapping.category.id.eq(categoryId)
                )
                .limit(5)
                .orderBy(recipe.totalLike.desc(), recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> recipesOrderByFollow(Integer pageIndex, Integer pageSize, Member member, BooleanExpression... booleanExpressions) {

        BooleanExpression combinedExpression = null;

        for (BooleanExpression expression : booleanExpressions) {
            if (combinedExpression == null) {
                combinedExpression = expression;
            } else {
                combinedExpression = combinedExpression.and(expression);
            }
        }

        List<Recipe> content = new ArrayList<>();

        //팔로잉 레시피 갯수 계산(일주일 전 것까지)
        Long followingCount = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(
                        combinedExpression
                                .and(blockedMemberNotInForRecipe(member))
                                .and(getFollowerRecipeCondition(member))
                )
                .fetchOne();


        if (followingCount >= (pageIndex+1) * pageSize) {
            log.info("if로 넘어옴");
            //index를 넘지 않으면 팔로잉 레시피 먼저
            content = queryFactory
                    .selectFrom(recipe)
                    .where(
                            combinedExpression
                                    .and(blockedMemberNotInForRecipe(member))
                                    .and(getFollowerRecipeCondition(member))
                    )
                    .orderBy(recipe.createdAt.desc())
                    .offset(pageIndex * pageSize)
                    .limit(pageSize)
                    .fetch();

        } else if (followingCount > pageIndex * pageSize) {
            log.info("else if로 넘어옴");
            //index에 끼어있으면 팔로잉,일반 레시피 둘 다. offset과 pagesize 잘 계산해야함

            //팔로잉 추가
            content.addAll(queryFactory
                    .selectFrom(recipe)
                    .where(
                            combinedExpression
                                    .and(blockedMemberNotInForRecipe(member))
                                    .and(getFollowerRecipeCondition(member))
                    )
                    .orderBy(recipe.createdAt.desc())
                    .offset(pageIndex * pageSize)
                    .limit(pageSize)
                    .fetch());

            content.addAll(queryFactory
                    .selectFrom(recipe)
                    .where(
                            combinedExpression
                                    .and(blockedMemberNotInForRecipe(member))
                                    .and(getNotFollowerRecipeCondition(member))
                    )
                    .orderBy(recipe.createdAt.desc())
                    .offset(0)
                    .limit(pageSize - content.size())
                    .fetch());

        } else {
            log.info("else로 넘어옴");
            //일반 레시피만. offset 잘 계산해야함
            content = queryFactory
                    .selectFrom(recipe)
                    .where(
                            combinedExpression
                                    .and(blockedMemberNotInForRecipe(member))
                                    .and(getNotFollowerRecipeCondition(member))
                    )
                    .orderBy(recipe.createdAt.desc())
                    .offset(pageIndex == 0 ? 0 : pageIndex * pageSize - followingCount)
                    .limit(pageSize)
                    .fetch();

            log.info(content.toString());
        }

        log.info(content.toString());

        return content;
    }

    @Override
    public List<Recipe> recipesOrderBy(Integer pageIndex, Integer pageSize, Member member, String order, BooleanExpression... booleanExpressions) {

        BooleanExpression combinedExpression = null;

        for (BooleanExpression expression : booleanExpressions) {
            if (combinedExpression == null) {
                combinedExpression = expression;
            } else {
                combinedExpression = combinedExpression.and(expression);
            }
        }

        return queryFactory
                .selectFrom(recipe)
                .where(
                        combinedExpression
                                .and(blockedMemberNotInForRecipe(member))
                )
                .orderBy(order(order, member), recipe.createdAt.desc())
                .offset(pageIndex * pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long recipeTotalCount(Member member, BooleanExpression... booleanExpressions) {

        BooleanExpression combinedExpression = null;

        for (BooleanExpression expression : booleanExpressions) {
            if (combinedExpression == null) {
                combinedExpression = expression;
            } else {
                combinedExpression = combinedExpression.and(expression);
            }
        }

        return queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(
                        combinedExpression
                                .and(blockedMemberNotInForRecipe(member))
                )
                .fetchOne();
    }

    //--- BooleanExpression ---//

    @Override
    public BooleanExpression recipesInCategoryCondition(Long categoryId) {
        return recipe.in(queryFactory
                .select(recipeCategoryMapping.recipe)
                .from(recipeCategoryMapping)
                .where(recipeCategoryMapping.category.id.eq(categoryId))
                .fetch());
    }

    @Override
    public BooleanExpression recipesContainKeyword(String keyword) {
        return recipe.name.contains(keyword);
    }

    private BooleanExpression blockedMemberNotInForRecipe(Member member) {
        List<Member> blockedMember = getBlockedMember(member);

        return blockedMember.isEmpty() ? null : recipe.member.notIn(blockedMember);
    }

    private List<Member> getBlockedMember(Member member) {
        List<Member> blockedMember = blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());
        return blockedMember;
    }

    @Override
    public BooleanExpression checkWrittenBy(String writtenby) {
        if (writtenby.equals("barista"))
            return recipe.isBarista.eq(true);
        else if (writtenby.equals("common"))
            return recipe.isBarista.eq(false);
        else if (writtenby.equals("official"))
            return recipe.isOfficial.eq(true);
        else
            throw new RecipeException(CommonStatus.WRITTEN_BY_TYPE_ERROR);
    }

    private BooleanExpression getFollowerRecipeCondition(Member member) {
        List<Member> followee = queryFactory
                .select(follow.followee)
                .from(follow)
                .where(follow.follower.eq(member))
                .fetch();

        return followee.isEmpty() ? null : recipe.member.in(followee).and(recipe.createdAt.after(LocalDateTime.now().minusWeeks(1)));
    }
    private BooleanExpression getNotFollowerRecipeCondition(Member member) {
        Long count = queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.follower.eq(member))
                .fetchOne();

        List<Recipe> blacklist = queryFactory
                .selectFrom(recipe)
                .where(getFollowerRecipeCondition(member))
                .fetch();

        log.info("blacklist count: ", blacklist);

        return count == 0 ? null : recipe.notIn(blacklist);
    }

    //--- OrderSpecifier ---//

    private OrderSpecifier order(String order, Member member) {

        if(order.equals("likes"))
            return new OrderSpecifier<>(Order.DESC, recipe.totalLike);
        else if(order.equals("latest") || order.equals("follow"))
            return new OrderSpecifier(Order.DESC, recipe.createdAt);
        else
            throw new RecipeException(CommonStatus.ORDER_BY_TYPE_ERROR);
    }
}
