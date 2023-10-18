package zipdabang.server.repository.recipeRepositories.recipeRepositoryImpl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.QTempRecipe;
import zipdabang.server.domain.recipe.TempRecipe;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.TempRecipeRepositoryCustom;

import java.util.List;

import static zipdabang.server.domain.recipe.QTempRecipe.tempRecipe;

@RequiredArgsConstructor
public class TempRecipeRepositoryImpl implements TempRecipeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QTempRecipe qTempRecipe = tempRecipe;

    @Override
    public List<TempRecipe> getTempRecipePageList(Integer pageIndex, Integer pageSize, Member member) {
        return queryFactory
                .selectFrom(tempRecipe)
                .where(
                        tempRecipe.member.eq(member)
                )
                .orderBy(tempRecipe.updatedAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Long getTempRecipeTotalCount(Member member) {
        return queryFactory
                .select(tempRecipe.count())
                .from(tempRecipe)
                .where(tempRecipe.member.eq(member)
                )
                .fetchOne();
    }
}
