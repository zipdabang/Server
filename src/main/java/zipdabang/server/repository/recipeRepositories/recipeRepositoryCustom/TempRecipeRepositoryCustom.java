package zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom;

import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.TempRecipe;

import java.util.List;

public interface TempRecipeRepositoryCustom {

    List<TempRecipe> getTempRecipePageList(Integer pageIndex, Integer pageSize, Member member);

    Long getTempRecipeTotalCount(Member member);
}
