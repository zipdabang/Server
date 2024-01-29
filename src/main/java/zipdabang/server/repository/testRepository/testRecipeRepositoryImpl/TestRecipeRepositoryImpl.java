package zipdabang.server.repository.testRepository.testRecipeRepositoryImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.test.QTestRecipe;
import zipdabang.server.domain.test.QTestRecipeCategoryMapping;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.repository.testRepository.testRecipeRepositoryCustom.TestRecipeRepositoryCustom;

import java.util.List;

import static zipdabang.server.domain.recipe.QRecipe.recipe;
import static zipdabang.server.domain.recipe.QRecipeCategoryMapping.recipeCategoryMapping;
import static zipdabang.server.domain.test.QTestRecipe.testRecipe;
import static zipdabang.server.domain.test.QTestRecipeCategoryMapping.testRecipeCategoryMapping;

@Slf4j
@RequiredArgsConstructor
public class TestRecipeRepositoryImpl implements TestRecipeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QTestRecipe qTestRecipe = testRecipe;
    QTestRecipeCategoryMapping qTestRecipeCategoryMapping = testRecipeCategoryMapping;

    @Override
    public BooleanExpression recipesInCategoryCondition(Long categoryId) {
        return testRecipe.in(queryFactory
                .select(testRecipeCategoryMapping.recipe)
                .from(testRecipeCategoryMapping)
                .where(testRecipeCategoryMapping.category.id.eq(categoryId))
                .fetch());
    }

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
                .orderBy(testRecipe.createdAt.desc())
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
                .select(testRecipe.count())
                .from(testRecipe)
                .where(
                        combinedExpression
                )
                .fetchOne();
    }
}
