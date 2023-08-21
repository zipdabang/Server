package zipdabang.server.service;

import zipdabang.server.domain.market.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;

public interface RecipeService {

    Recipe create(RecipeRequestDto.CreateRecipeDto request, Member member)throws IOException;
}
