package zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom;

import com.querydsl.core.types.dsl.BooleanExpression;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;

import java.util.List;

public interface RecipeRepositoryCustom {

    List<Recipe> getWrittenByPreview(String writtenby, Member member, Integer previewSize);

    List<Recipe> getTop5RecipePerCategory(Long categoryId);

    List<Recipe> recipesOrderByFollow(Integer pageIndex, Integer pageSize, Member member, BooleanExpression... booleanExpressions);

    List<Recipe> recipesOrderBy(Integer pageIndex, Integer pageSize, Member member, String order, BooleanExpression... booleanExpressions);

    Long recipeTotalCount(Member member, BooleanExpression... booleanExpressions);

    BooleanExpression recipesInCategoryCondition(Long categoryId);

    BooleanExpression recipesContainKeyword(String keyword);
}
