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
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.RecipeException;
import zipdabang.server.config.AmazonConfig;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.File;
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
    private final RecipeBannerRepository recipeBannerRepository;
    private final StepRepository stepRepository;
    private final IngredientRepository ingredientRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;
    private final AmazonS3Manager amazonS3Manager;

    private final BlockedMemberRepository blockedMemberRepository;
    private final CommentRepository commentRepository;
    private final BlockedCommentRepository blockedCommentRepository;

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

    @Transactional(readOnly = false)
    @Override
    public Recipe getRecipe(Long recipeId, Member member) {

        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(Code.NO_RECIPE_EXIST));
        Optional<BlockedMember> blockedInfos= blockedMemberRepository.findByOwnerAndBlocked(member, findRecipe.getMember());

        if(blockedInfos.isPresent()){
            throw new RecipeException(Code.BLOCKED_USER_RECIPE);
        }
        else {
            findRecipe.updateView();
            return findRecipe;
        }

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

        List<Member> blockedMember = getBlockedMembers(member);

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

        List<Member> blockedMember = getBlockedMembers(member);

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

        List<Member> blockedMember = getBlockedMembers(member);

        List<Long> recipeIdList = new ArrayList<>();

        if (categoryId == 0){
            recipeIdList = recipeRepository.findAll().stream()
                    .map(recipe -> recipe.getId())
                    .collect(Collectors.toList());
        }
        else{
            List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

            if(recipeCategory.isEmpty())
                throw new RecipeException(Code.RECIPE_NOT_FOUND);

            recipeIdList = recipeCategoryMappingRepository.findByCategoryIn(recipeCategory).stream()
                    .map(categoryMapping -> categoryMapping.getRecipe().getId())
                    .collect(Collectors.toList());
        }

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
    public List<List<Recipe>> searchRecipePreview(String keyword, Member member) {
        Long recipeCategorySize = recipeCategoryRepository.count()-1;

        List<Member> blockedMember = getBlockedMembers(member);

        List<List<Recipe>> recipeList = new ArrayList<>();

        for(Long categoryId = 1L; categoryId <= recipeCategorySize; categoryId++) {
            List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

            List<Long> recipeIdList = recipeCategoryMappingRepository.findByCategoryIn(recipeCategory).stream()
                    .map(categoryMapping -> categoryMapping.getRecipe().getId())
                    .collect(Collectors.toList());

            if (blockedMember.isEmpty())
                recipeList.add(recipeRepository.findTop5ByIdInAndNameContainingOrderByCreatedAtDesc(recipeIdList, keyword));
            else
                recipeList.add(recipeRepository.findTop5ByIdInAndNameContainingAndMemberNotInOrderByCreatedAtDesc(recipeIdList, keyword, blockedMember));
        }

        return recipeList;
    }

    private List<Member> getBlockedMembers(Member member) {
        List<Member> blockedMember = blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());
        return blockedMember;
    }

    public List<RecipeBanner> getRecipeBannerList() {
        return recipeBannerRepository.findAll();
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteRecipe(Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        if (findRecipe.getMember().equals(member)) {
            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(findRecipe.getThumbnailUrl()).substring(1));
            stepRepository.findAllByRecipeId(recipeId).stream()
                    .forEach(step -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(step.getImageUrl()).substring(1)));
            recipeRepository.deleteById(recipeId);
        }
        else
            throw new RecipeException(Code.NOT_RECIPE_OWNER);

        return recipeRepository.existsById(recipeId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Comment createComment(String content, Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        Comment buildComment = RecipeConverter.toComment(content, findRecipe, member);
        return commentRepository.save(buildComment);
    }

    @Override
    public Page<Comment> commentList(Integer pageIndex, Long recipeId, Member member) {
        recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        List<Member> blockedMember = getBlockedMembers(member);
        List<Long> blockedComment = getBlockedComment(member);

        if(blockedMember.isEmpty() && blockedComment.isEmpty())
            return commentRepository.findAll(
                    PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        else if(!blockedMember.isEmpty() && blockedComment.isEmpty())
            return commentRepository.findByMemberNotIn(blockedMember, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        else if(blockedMember.isEmpty() && !blockedComment.isEmpty())
            return commentRepository.findByIdNotIn(blockedComment, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        else
            return commentRepository.findByIdNotInAndMemberNotIn(blockedComment, blockedMember, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    private List<Long> getBlockedComment(Member member) {

        List<Long> blockedCommentIdList = blockedCommentRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked().getId())
                .collect(Collectors.toList());

        return blockedCommentIdList;
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteComment(Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(Code.NO_COMMENT_EXIST));

        if (findComment.getMember().equals(member) && findComment.getRecipe().equals(findRecipe)) {
            commentRepository.deleteById(commentId);
        }
        else
            throw new RecipeException(Code.NOT_COMMENT_OWNER);

        return commentRepository.existsById(recipeId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Comment updateComment(RecipeRequestDto.updateCommentDto request, Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(Code.NO_COMMENT_EXIST));


        if (findComment.getMember().equals(member) && findComment.getRecipe().equals(findRecipe)) {
            return findComment.updateContent(request.getComment());
        }
        else
            throw new RecipeException(Code.NOT_COMMENT_OWNER);
    }
}
