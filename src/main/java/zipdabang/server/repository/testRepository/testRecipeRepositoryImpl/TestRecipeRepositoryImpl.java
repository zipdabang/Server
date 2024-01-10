package zipdabang.server.repository.testRepository.testRecipeRepositoryImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.test.QTestRecipe;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.repository.testRepository.testRecipeRepositoryCustom.TestRecipeRepositoryCustom;

import java.util.List;

import static zipdabang.server.domain.recipe.QRecipe.recipe;
import static zipdabang.server.domain.test.QTestRecipe.testRecipe;

@Slf4j
@RequiredArgsConstructor
public class TestRecipeRepositoryImpl implements TestRecipeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QTestRecipe qTestRecipe = testRecipe;

    @Override
    public List<TestRecipe> testRecipesOrderBy(Integer pageIndex, Integer pageSize, String order, BooleanExpression... booleanExpressions) {

        BooleanExpression combinedExpression = null;

        for (BooleanExpression expression : booleanExpressions) {
            if (combinedExpression == null) {
                combinedExpression = expression;
            } else {
                combinedExpression = combinedExpression.and(expression);
            }
        }

        return queryFactory
                .selectFrom(testRecipe)
                .orderBy(recipe.createdAt.desc())
                .offset(pageIndex * pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long testRecipeTotalCount(BooleanExpression... booleanExpressions) {

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
                )
                .fetchOne();
    }
}
