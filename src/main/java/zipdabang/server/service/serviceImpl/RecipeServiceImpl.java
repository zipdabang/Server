package zipdabang.server.service.serviceImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.RecipeException;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.repository.ReportRepository;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static zipdabang.server.domain.recipe.QComment.comment;
import static zipdabang.server.domain.recipe.QRecipe.recipe;
import static zipdabang.server.domain.recipe.QRecipeCategoryMapping.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final TempRecipeRepository tempRecipeRepository;
    private final RecipeCategoryMappingRepository recipeCategoryMappingRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final RecipeBannerRepository recipeBannerRepository;
    private final StepRepository stepRepository;
    private final TempStepRepository tempStepRepository;
    private final IngredientRepository ingredientRepository;
    private final TempIngredientRepository tempIngredientRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;
    private final AmazonS3Manager amazonS3Manager;

    private final BlockedMemberRepository blockedMemberRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final ReportedRecipeRepository reportedRecipeRepository;

    private final JPAQueryFactory queryFactory;


    @Value("${paging.size}")
    Integer pageSize;

    @Value("5")
    Integer previewSize;

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
    @Transactional(readOnly = false)
    public TempRecipe tempCreate(RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException {

        log.info("service: ", request.toString());

        TempRecipe buildTempRecipe = RecipeConverter.toTempRecipe(request, thumbnail, member);
        TempRecipe tempRecipe = tempRecipeRepository.save(buildTempRecipe);

        if (request.getStepCount() > 0) {
            RecipeConverter.toTempStep(request, tempRecipe, stepImages).stream()
                    .map(step -> tempStepRepository.save(step))
                    .collect(Collectors.toList())
                    .stream()
                    .map(step -> step.setTempRecipe(tempRecipe));
        }

        if(request.getIngredientCount() > 0) {
            RecipeConverter.toTempIngredient(request, tempRecipe).stream()
                    .map(ingredient -> tempIngredientRepository.save(ingredient))
                    .collect(Collectors.toList())
                    .stream()
                    .map(ingredient -> ingredient.setTempRecipe(tempRecipe));
        }

        return tempRecipe;
    }

    @Override
    @Transactional(readOnly = false)
    public TempRecipe tempUpdate(Long tempId, RecipeRequestDto.TempRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException {

        log.info("service: ", request.toString());

        TempRecipe tempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(Code.NO_TEMP_RECIPE_EXIST));

        //recipe
        String thumbnailUrl = null;
        if (request.getThumbnailUrl() == null){
            if (tempRecipe.getThumbnailUrl() != null)
                amazonS3Manager.deleteFile(RecipeConverter.toKeyName(tempRecipe.getThumbnailUrl()).substring(1));

            if(thumbnail != null)
                thumbnailUrl = RecipeConverter.uploadThumbnail(thumbnail);

        }
        else {
            thumbnailUrl = request.getThumbnailUrl();
        }

        tempRecipe.setThumbnail(thumbnailUrl);
        tempRecipe.updateInfo(request);


        //step
        if(request.getStepCount() > 0) {
            RecipeConverter.toTempStep(request, tempRecipe, stepImages).stream()
                    .map(step -> tempStepRepository.save(step))
                    .collect(Collectors.toList())
                    .stream()
                    .map(step -> step.setTempRecipe(tempRecipe));
        }
        else{
            tempStepRepository.findAllByTempRecipe(tempRecipe).stream()
                    .filter(step -> step.getImageUrl() != null)
                    .forEach(step -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(step.getImageUrl()).substring(1)));
            tempStepRepository.deleteAllByTempRecipe(tempRecipe);
        }

        //ingredient
        if(request.getIngredientCount() >0) {
            tempIngredientRepository.deleteAllByTempRecipe(tempRecipe);

            RecipeConverter.toTempIngredient(request, tempRecipe).stream()
                    .map(ingredient -> tempIngredientRepository.save(ingredient))
                    .collect(Collectors.toList())
                    .stream()
                    .map(ingredient -> ingredient.setTempRecipe(tempRecipe));
        }
        else{
            tempIngredientRepository.deleteAllByTempRecipe(tempRecipe);
        }

        return tempRecipe;

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

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(Code.RECIPE_NOT_FOUND);

        QRecipe qRecipe = recipe;
        QRecipeCategoryMapping qRecipeCategoryMapping = recipeCategoryMapping;

        List<Recipe> content = queryFactory
                .selectFrom(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping).fetchJoin()
                .where(blockedMemberNotInForRecipe(member),
                        recipe.name.contains(keyword),
                        recipeCategoryMapping.category.id.eq(categoryId)
                )
                .orderBy(recipe.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(recipe.count())
                .from(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping)
                .where(blockedMemberNotInForRecipe(member),
                        recipe.name.contains(keyword),
                        recipeCategoryMapping.category.id.eq(categoryId)
                );

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count.fetchOne());
    }

    @Override
    public List<Recipe> getWrittenByRecipePreview(String writtenby, Member member) {

        QRecipe qRecipe = recipe;

        List<Recipe> recipeList = queryFactory
                .selectFrom(recipe)
                .where(blockedMemberNotInForRecipe(member),
                        checkWrittenBy(writtenby)
                        )
                .limit(previewSize)
                .orderBy(recipe.createdAt.desc())
                .fetch();

        log.info(recipeList.toString());

        return recipeList;
    }

    private BooleanExpression checkWrittenBy(String writtenby) {
        if (writtenby.equals("influencer"))
            return recipe.isInfluencer.eq(true);
        else if (writtenby.equals("common"))
            return recipe.isInfluencer.eq(false);
        else if (writtenby.equals("all"))
            return null;
        else
            throw new RecipeException(Code.WRITTEN_BY_TYPE_ERROR);
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
    @Transactional(readOnly = false)
    public Recipe updateLikeOnRecipe(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));

        if(recipe.getMember() == member)
            throw new RecipeException(Code.RECIPE_OWNER);

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

        if(recipe.getMember() == member)
            throw new RecipeException(Code.RECIPE_OWNER);

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
    public List<Recipe> getTop5RecipePerCategory(Long categoryId) {
        QRecipe qRecipe = recipe;
        QRecipeCategoryMapping qRecipeCategoryMapping = recipeCategoryMapping;

        AtomicLong index = new AtomicLong(1);
        List<Recipe> recipeList = queryFactory
                .selectFrom(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping).fetchJoin()
                .where(
                        recipeCategoryMapping.category.id.eq(categoryId)
                )
                .limit(5)
                .orderBy(recipe.totalLike.desc(), recipe.createdAt.desc())
                .fetch();

        log.info(recipeList.toString());

        return recipeList;
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
        List<Member> blockedMember = getBlockedMember(member);

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
    public Long reportRecipe(Long recipeId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(Code.NO_REPORT_EXIST));

        if (!findRecipe.getMember().equals(member)) {
            ReportedRecipe mapping = RecipeConverter.toRecipeReport(findReport, findRecipe, member);
            reportedRecipeRepository.save(mapping);

            return findRecipe.getId();
        }
        else
            throw new RecipeException(Code.RECIPE_OWNER);
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

        QComment qComment = comment;

        List<Comment> content = queryFactory
                .selectFrom(comment)
                .where(blockedMemberNotInForComment(member))
                .orderBy(comment.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();


        JPAQuery<Long> count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(blockedMemberNotInForComment(member));

        return PageableExecutionUtils.getPage(content,PageRequest.of(pageIndex,pageSize), ()->count.fetchOne());
    }

    private BooleanExpression blockedMemberNotInForComment(Member member) {
        List<Member> blockedMember = getBlockedMember(member);

        return blockedMember.isEmpty() ? null : comment.member.notIn(blockedMember);
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

    @Transactional(readOnly = false)
    @Override
    public Long reportComment(Long recipeId, Long commentId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(Code.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(Code.NO_COMMENT_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(Code.NO_REPORT_EXIST));

        if (!findComment.getMember().equals(member) && findComment.getRecipe().equals(findRecipe)) {
            ReportedComment mapping = RecipeConverter.toCommentReport(findReport, findComment, member);
            reportedCommentRepository.save(mapping);

            return findComment.getId();
        }
        else
            throw new RecipeException(Code.COMMENT_OWNER);
    }
}
