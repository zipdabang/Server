package zipdabang.server.repository.testRepository.testRecipeRepositoryCustom;

import com.querydsl.core.types.dsl.BooleanExpression;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.test.TestRecipe;

import java.util.List;

public interface TestRecipeRepositoryCustom {

    List<TestRecipe> testRecipesOrderBy(Integer pageIndex, Integer pageSize, String order, BooleanExpression... booleanExpressions);

    Long testRecipeTotalCount(BooleanExpression... booleanExpressions);

}
