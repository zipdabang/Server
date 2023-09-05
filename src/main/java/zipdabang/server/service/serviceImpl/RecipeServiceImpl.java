package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.RecipeException;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.domain.recipe.Scrap;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryMappingRepository recipeCategoryMappingRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final StepRepository stepRepository;
    private final IngredientRepository ingredientRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;

    private final BlockedMemberRepository blockedMemberRepository;

    @Value("${paging.size}")
    Integer pageSize;

    @Override
    @Transactional(readOnly = false)
    public Recipe create(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException {

        log.info("service: ", request.toString());

        Recipe buildRecipe = RecipeConverter.toRecipe(request, thumbnail, member);
        Recipe recipe = recipeRepository.save(buildRecipe);

        RecipeConverter.toRecipeCategory(request,recipe).stream()
                .map(categoryMapping -> recipeCategoryMappingRepository.save(categoryMapping))
                .collect(Collectors.toList())
                .stream()
                .map(categoryMapping -> categoryMapping.setRecipe(recipe));


        RecipeConverter.toStep(request, recipe, stepImages).stream()
                .map(step -> stepRepository.save(step))
                .collect(Collectors.toList())
                .stream()
                .map(step -> step.setRecipe(recipe));

        RecipeConverter.toIngredient(request, recipe).stream()
                .map(ingredient -> ingredientRepository.save(ingredient))
                .collect(Collectors.toList())
                .stream()
                .map(ingredient -> ingredient.setRecipe(recipe));

        return recipe;
    }

    @Override
    public Recipe getRecipe(Long recipeId, Member member) {

        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(Code.NO_RECIPE_EXIST));
        Optional<BlockedMember> blockedInfos= blockedMemberRepository.findByOwnerAndBlocked(member, findRecipe.getMember());

        if(blockedInfos.isPresent()){
            throw new RecipeException(Code.BLOCKED_USER_RECIPE);
        }
        else {
            return findRecipe;
        }

//        List<Member> blockedMemberList = blockedInfos.stream()
//                .map(blockedMember -> blockedMember.getBlocked())
//                .collect(Collectors.toList());
//        Recipe findRecipe = recipeRepository.findById(recipeId).get();

    }

    @Override
    public Boolean checkOwner(Recipe recipe, Member member) {
        if (recipe.getMember() == member)
            return true;
        else
            return false;
    }

    @Override
    public Boolean getLike(Recipe recipe, Member member) {
        return likesRepository.findByRecipeAndMember(recipe,member).isPresent();
    }

    @Override
    public Boolean getScrap(Recipe recipe, Member member) {
        return scrapRepository.findByRecipeAndMember(recipe, member).isPresent();
    }

    @Override
    public Page<Recipe> searchRecipe(Long categoryId, String keyword, Integer pageIndex, Member member) {

        List<Member> blockedMember= blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(Code.RECIPE_NOT_FOUND);

        List<Long> recipeIdList  = recipeCategoryMappingRepository.findByCategoryIn(recipeCategory).stream()
                .map(categoryMapping -> categoryMapping.getRecipe().getId())
                .collect(Collectors.toList());

        if(blockedMember.isEmpty())
            return recipeRepository.findByIdInAndNameContaining(recipeIdList, keyword,
                    PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        else
            return recipeRepository.findByIdInAndNameContainingAndMemberNotIn(recipeIdList, keyword,blockedMember,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

    }

    @Override
    public List<Recipe> getWrittenByRecipePreview(String writtenby, Member member) {
        List<Recipe> recipeList = new ArrayList<>();

        List<Member> blockedMember= blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());

        if (!blockedMember.isEmpty()) {
            if (writtenby.equals("all")) {
                recipeList = recipeRepository.findTop5ByMemberNotInOrderByCreatedAtDesc(blockedMember);
                log.info("all: ", recipeList.toString());
            } else if (writtenby.equals("influencer")) {
                recipeList = recipeRepository.findTop5ByIsInfluencerTrueAndMemberNotInOrderByCreatedAtDesc(blockedMember);
                log.info("influencer: ", recipeList.toString());
            } else if (writtenby.equals("common")) {
                recipeList = recipeRepository.findTop5ByIsInfluencerFalseAndMemberNotInOrderByCreatedAtDesc(blockedMember);
                log.info("common: ", recipeList.toString());
            } else
                throw new RecipeException(Code.WRITTEN_BY_TYPE_ERROR);
        }
        else{
            if (writtenby.equals("all")) {
                recipeList = recipeRepository.findTop5ByOrderByCreatedAtDesc();
                log.info("all: ", recipeList.toString());
            } else if (writtenby.equals("influencer")) {
                recipeList = recipeRepository.findTop5ByIsInfluencerTrueOrderByCreatedAtDesc();
                log.info("influencer: ", recipeList.toString());
            } else if (writtenby.equals("common")) {
                recipeList = recipeRepository.findTop5ByIsInfluencerFalseOrderByCreatedAtDesc();
                log.info("common: ", recipeList.toString());
            } else
                throw new RecipeException(Code.WRITTEN_BY_TYPE_ERROR);
        }

        return recipeList;
    }

    @Override
    @Transactional(readOnly = false)
    public Recipe updateLikeOnRecipe(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        Optional<Likes> likesExist = likesRepository.findByRecipeAndMember(recipe,member);

        if(likesExist.isEmpty()) {
            Likes savedLikes = likesRepository.save(RecipeConverter.toLikes(recipe, member));
            savedLikes.setRecipe(recipe);
        }
        else{
            likesExist.get().deleteLikes(recipe);
            likesRepository.deleteById(likesExist.get().getId());
        }

        return recipe;
    }

    @Override
    @Transactional(readOnly = false)
    public Recipe updateScrapOnRecipe(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        Optional<Scrap> scrapExist = scrapRepository.findByRecipeAndMember(recipe,member);

        if(scrapExist.isEmpty()) {
            Scrap savedScrap = scrapRepository.save(RecipeConverter.toScrap(recipe, member));
            savedScrap.setRecipe(recipe);
        }
        else {
            scrapExist.get().deleteScrap(recipe);
            scrapRepository.deleteById(scrapExist.get().getId());
        }

        return recipe;
    }

    @Override
    public List<RecipeCategory> getAllRecipeCategories() {
        return recipeCategoryRepository.findAll();
    }

    @Override
    public Page<Recipe> recipeListByCategory(Long categoryId, Integer pageIndex, Member member, String order) {

        List<Member> blockedMember= blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(Code.RECIPE_NOT_FOUND);

        List<Long> recipeIdList  = recipeCategoryMappingRepository.findByCategoryIn(recipeCategory).stream()
                .map(categoryMapping -> categoryMapping.getRecipe().getId())
                .collect(Collectors.toList());

        String orderBy = null;

        if(order == null)
            order = "latest";

        if(order.equals("likes"))
            orderBy = "totalLike";
        else if(order.equals("views"))
            orderBy = "totalView";
        else if(order.equals("latest"))
            orderBy = "createdAt";
        else
            throw new RecipeException(Code.ORDER_BY_TYPE_ERROR);

        if(blockedMember.isEmpty())
            return recipeRepository.findByIdIn(recipeIdList,
                    PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, orderBy)));
        else
            return recipeRepository.findByIdInAndMemberNotIn(recipeIdList,blockedMember,
                    PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, orderBy)));
    }

    @Override
    public boolean checkRecipeCategoryExist(Long categoryId) {
        return recipeCategoryRepository.existsById(categoryId);
    }

    @Override
    public List<Recipe> searchRecipePreview(Long categoryId, String keyword, Member member) {
        List<Member> blockedMember= blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(Code.RECIPE_NOT_FOUND);

        List<Long> recipeIdList  = recipeCategoryMappingRepository.findByCategoryIn(recipeCategory).stream()
                .map(categoryMapping -> categoryMapping.getRecipe().getId())
                .collect(Collectors.toList());

        if(blockedMember.isEmpty())
            return recipeRepository.findTop5ByIdInAndNameContainingOrderByCreatedAtDesc(recipeIdList, keyword);
        else
            return recipeRepository.findTop5ByIdInAndNameContainingAndMemberNotInOrderByCreatedAtDesc(recipeIdList,keyword,blockedMember);
    }
}
