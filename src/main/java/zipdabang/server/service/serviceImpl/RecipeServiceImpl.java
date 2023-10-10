package zipdabang.server.service.serviceImpl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
import zipdabang.server.apiPayload.code.RecipeStatus;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.MemberException;
import zipdabang.server.aws.s3.AmazonS3Manager;

import zipdabang.server.apiPayload.exception.handler.RecipeException;

import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.QFollow;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.repository.ReportRepository;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static zipdabang.server.domain.member.QFollow.follow;
import static zipdabang.server.domain.recipe.QComment.comment;
import static zipdabang.server.domain.recipe.QRecipe.recipe;
import static zipdabang.server.domain.recipe.QRecipeCategoryMapping.*;
import static zipdabang.server.domain.recipe.QTempRecipe.tempRecipe;

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

    private final MemberRepository memberRepository;
    private final BlockedMemberRepository blockedMemberRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final ReportedRecipeRepository reportedRecipeRepository;
    private final WeeklyBestRecipeRepository weeklyBestRecipeRepository;

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

        RecipeConverter.toRecipeCategory(request.getCategoryId(),recipe).stream()
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
    public Recipe update(Long recipeId, RecipeRequestDto.UpdateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages, Member member) throws IOException {
        log.info("service: ", request.toString());

        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));

        if(!recipe.getMember().equals(member))
            throw new RecipeException(RecipeStatus.NOT_RECIPE_OWNER);

        recipeCategoryMappingRepository.deleteAllByRecipe(recipe);

        RecipeConverter.toRecipeCategory(request.getCategoryId(),recipe).stream()
                .map(categoryMapping -> recipeCategoryMappingRepository.save(categoryMapping))
                .collect(Collectors.toList())
                .stream()
                .map(categoryMapping -> categoryMapping.setRecipe(recipe));

        //recipe
        String thumbnailUrl = null;
        if (thumbnail != null) {
            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(recipe.getThumbnailUrl()).substring(1));
            thumbnailUrl = RecipeConverter.uploadThumbnail(thumbnail);
            recipe.setThumbnail(thumbnailUrl);
        }

        recipe.updateInfo(request);


        //step
        List<String> presentImageUrls = stepRepository.findAllByRecipeId(recipeId).stream()
                .filter(steps -> steps.getImageUrl() != null)
                .map(tempStep -> tempStep.getImageUrl())
                .collect(Collectors.toList());

        stepRepository.deleteAllByRecipe(recipe);


        RecipeConverter.toUpdateStep(request, recipe, stepImages, presentImageUrls).stream()
                .map(step -> stepRepository.save(step))
                .collect(Collectors.toList())
                .stream()
                .map(step -> step.setRecipe(recipe));

        if(!presentImageUrls.isEmpty())
            presentImageUrls.forEach(imageUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(imageUrl).substring(1)));

        //ingredient
        ingredientRepository.deleteAllByRecipe(recipe);

        RecipeConverter.toUpdateIngredient(request, recipe).stream()
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
        List<String> presentImageUrls = new ArrayList<>();


        if (request.getStepCount() > 0) {
            RecipeConverter.toTempStep(request, tempRecipe, stepImages, presentImageUrls).stream()
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

        TempRecipe tempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_TEMP_RECIPE_EXIST));

        //recipe
        String thumbnailUrl = null;
        if (thumbnail != null) {
            if (tempRecipe.getThumbnailUrl() != null)
                amazonS3Manager.deleteFile(RecipeConverter.toKeyName(tempRecipe.getThumbnailUrl()).substring(1));
            thumbnailUrl = RecipeConverter.uploadThumbnail(thumbnail);
        }
        else{
            if (request.getThumbnailUrl() == null && tempRecipe.getThumbnailUrl() != null)
                amazonS3Manager.deleteFile(RecipeConverter.toKeyName(tempRecipe.getThumbnailUrl()).substring(1));
            else if (request.getThumbnailUrl() != null)
                thumbnailUrl = request.getThumbnailUrl();
        }

        tempRecipe.setThumbnail(thumbnailUrl);
        tempRecipe.updateInfo(request);


        //step
        List<String> presentImageUrls = tempStepRepository.findAllByTempRecipe(tempRecipe).stream()
                .filter(steps -> steps.getImageUrl() != null)
                .map(tempStep -> tempStep.getImageUrl())
                .collect(Collectors.toList());

        tempStepRepository.deleteAllByTempRecipe(tempRecipe);

        if(request.getStepCount() > 0) {
            RecipeConverter.toTempStep(request, tempRecipe, stepImages, presentImageUrls).stream()
                    .map(step -> tempStepRepository.save(step))
                    .collect(Collectors.toList())
                    .stream()
                    .map(step -> step.setTempRecipe(tempRecipe));
        }
        if(!presentImageUrls.isEmpty())
            presentImageUrls.forEach(imageUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(imageUrl).substring(1)));

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

    @Override
    public TempRecipe getTempRecipe(Long tempId) {
        return tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_TEMP_RECIPE_EXIST));
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean deleteTempRecipe(Long tempId, Member member) {

        TempRecipe findTempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_TEMP_RECIPE_EXIST));

        if (findTempRecipe.getMember().equals(member)) {
            if(findTempRecipe.getThumbnailUrl() != null)
                amazonS3Manager.deleteFile(RecipeConverter.toKeyName(findTempRecipe.getThumbnailUrl()).substring(1));

            List<TempStep> tempSteps = tempStepRepository.findAllByTempRecipe(findTempRecipe);
            if (!tempSteps.isEmpty()) {
                    tempSteps.stream()
                        .forEach(step -> {
                                    if (step.getImageUrl() != null)
                                        amazonS3Manager.deleteFile(RecipeConverter.toKeyName(step.getImageUrl()).substring(1));
                                }
                        );
            }
            tempRecipeRepository.deleteById(tempId);
        }
        else
            throw new RecipeException(RecipeStatus.NOT_RECIPE_OWNER);

        return tempRecipeRepository.existsById(tempId) == false;
    }

    @Override
    @Transactional(readOnly = false)
    public Recipe createFromTempRecipe(Long tempId, RecipeRequestDto.RecipeCategoryList categoryList, Member member) {
        TempRecipe tempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_TEMP_RECIPE_EXIST));
        List<TempStep> tempSteps = tempStepRepository.findAllByTempRecipe(tempRecipe);
        List<TempIngredient> tempIngredients = tempIngredientRepository.findAllByTempRecipe(tempRecipe);

        Recipe recipe = recipeRepository.save(RecipeConverter.toRecipeFromTemp(tempRecipe, tempSteps, tempIngredients, member));

        RecipeConverter.toRecipeCategory(categoryList.getCategoryId(),recipe).stream()
                .map(categoryMapping -> recipeCategoryMappingRepository.save(categoryMapping))
                .collect(Collectors.toList())
                .stream()
                .map(categoryMapping -> categoryMapping.setRecipe(recipe));

        RecipeConverter.toStepFromTemp(tempSteps, recipe).stream()
                .map(step -> stepRepository.save(step))
                .collect(Collectors.toList())
                .stream()
                .map(step -> step.setRecipe(recipe));

        RecipeConverter.toIngredientFromTemp(tempIngredients, recipe).stream()
                .map(ingredient -> ingredientRepository.save(ingredient))
                .collect(Collectors.toList())
                .stream()
                .map(ingredient -> ingredient.setRecipe(recipe));

        tempRecipeRepository.deleteById(tempId);

        return recipe;
    }

    @Override
    public Page<TempRecipe> getTempRecipeList(Integer pageIndex, Member member) {

        QTempRecipe qTempRecipe = tempRecipe;

        List<TempRecipe> content = queryFactory
                .selectFrom(tempRecipe)
                .where(
                        tempRecipe.member.eq(member)
                )
                .orderBy(tempRecipe.updatedAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(tempRecipe.count())
                .from(tempRecipe)
                .where(tempRecipe.member.eq(member)
                );

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count.fetchOne());
    }

    @Transactional(readOnly = false)
    @Override
    public Recipe getRecipe(Long recipeId, Member member) {

        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        Optional<BlockedMember> blockedInfos= blockedMemberRepository.findByOwnerAndBlocked(member, findRecipe.getMember());

        if(blockedInfos.isPresent()){
            throw new RecipeException(RecipeStatus.BLOCKED_USER_RECIPE);
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
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);

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
        if (writtenby.equals("barista"))
            return recipe.isBarista.eq(true);
        else if (writtenby.equals("common"))
            return recipe.isBarista.eq(false);
        else if (writtenby.equals("official"))
            return recipe.isOfficial.eq(true);
        else
            throw new RecipeException(RecipeStatus.WRITTEN_BY_TYPE_ERROR);
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
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));

        if(recipe.getMember() == member)
            throw new RecipeException(RecipeStatus.RECIPE_OWNER);

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
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));

        if(recipe.getMember() == member)
            throw new RecipeException(RecipeStatus.RECIPE_OWNER);

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

        List<RecipeCategory> categoryList = recipeCategoryRepository.findAll();
        RecipeCategory all = categoryList.remove(0);
        categoryList.add(all);

        return categoryList;
    }

    @Override
    public List<Recipe> getTop5RecipePerCategory(Long categoryId) {

        QRecipe qRecipe = recipe;
        QRecipeCategoryMapping qRecipeCategoryMapping = recipeCategoryMapping;

        List<Recipe> recipeList = new ArrayList<>();


        recipeList = queryFactory
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

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(RecipeStatus.RECIPE_NOT_FOUND);

        QRecipe qRecipe = recipe;
        QRecipeCategoryMapping qRecipeCategoryMapping = recipeCategoryMapping;
        QFollow qFollow = follow;

        List<Recipe> content = queryFactory
                .selectFrom(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping).fetchJoin()
                .where(blockedMemberNotInForRecipe(member),
                        recipeCategoryMapping.category.id.eq(categoryId)
                )
                .orderBy(order(order, member), recipe.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(recipe.count())
                .from(recipe)
                .join(recipe.categoryMappingList, recipeCategoryMapping)
                .where(blockedMemberNotInForRecipe(member),
                        recipeCategoryMapping.category.id.eq(categoryId)
                );

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count.fetchOne());
    }

    private OrderSpecifier order(String order, Member member) {
        if(order == null)
            order = "latest";

        if(order.equals("likes"))
            return new OrderSpecifier<>(Order.DESC, recipe.totalLike);
        else if(order.equals("follow"))
            return new OrderSpecifier(Order.DESC, recipe.member.eq(queryFactory
                    .select(follow.followee)
                    .from(follow)
                    .where(
                            follow.follower.eq(member)
                    )
                    .orderBy(recipe.createdAt.desc())
                )
            );
        else if(order.equals("latest"))
            return new OrderSpecifier(Order.DESC, recipe.createdAt);
        else
            throw new RecipeException(RecipeStatus.ORDER_BY_TYPE_ERROR);
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

    @Override
    public List<WeeklyBestRecipe> WeekBestRecipe() {
        List<WeeklyBestRecipe> bestRecipes = weeklyBestRecipeRepository.findAll();

        return bestRecipes;
    }

    @Override
    public List<Recipe> getRecipeByOwnerPreview(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));

        QRecipe qRecipe = recipe;

        List<Recipe> recipeList = queryFactory
                .selectFrom(recipe)
                .where(
                        recipe.member.eq(findMember)
                )
                .limit(5)
                .orderBy(recipe.createdAt.desc())
                .fetch();

        return recipeList;
    }

    @Override
    public Page<Recipe> getRecipeByOwner(Integer pageIndex, Long memberId) {

        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));

        QRecipe qRecipe = recipe;

        List<Recipe> content = queryFactory
                .selectFrom(recipe)
                .where(recipe.member.eq(findMember))
                .orderBy(recipe.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();


        JPAQuery<Long> count = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(recipe.member.eq(findMember));

        return PageableExecutionUtils.getPage(content,PageRequest.of(pageIndex,pageSize), ()->count.fetchOne());
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
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));

        if (findRecipe.getMember().equals(member)) {
            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(findRecipe.getThumbnailUrl()).substring(1));
            stepRepository.findAllByRecipeId(recipeId).stream()
                    .forEach(step -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(step.getImageUrl()).substring(1)));
            recipeRepository.deleteById(recipeId);
        }
        else
            throw new RecipeException(RecipeStatus.NOT_RECIPE_OWNER);

        return recipeRepository.existsById(recipeId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Long reportRecipe(Long recipeId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(CommonStatus.NO_REPORT_EXIST));

        if (!findRecipe.getMember().equals(member)) {
            ReportedRecipe mapping = RecipeConverter.toRecipeReport(findReport, findRecipe, member);
            reportedRecipeRepository.save(mapping);

            return findRecipe.getId();
        }
        else
            throw new RecipeException(RecipeStatus.RECIPE_OWNER);
    }

    @Transactional(readOnly = false)
    @Override
    public Comment createComment(String content, Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        findRecipe.updateComment(1);

        Comment buildComment = RecipeConverter.toComment(content, findRecipe, member);
        return commentRepository.save(buildComment);
    }

    @Override
    public Page<Comment> commentList(Integer pageIndex, Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));

        QComment qComment = comment;

        List<Comment> content = queryFactory
                .selectFrom(comment)
                .where(blockedMemberNotInForComment(member),
                        comment.recipe.eq(findRecipe))
                .orderBy(comment.createdAt.desc())
                .offset(pageIndex*pageSize)
                .limit(pageSize)
                .fetch();


        JPAQuery<Long> count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(blockedMemberNotInForComment(member),
                        comment.recipe.eq(findRecipe)
                );

        if (count.fetchOne() == 0)
            throw new RecipeException(RecipeStatus.COMMENT_NOT_FOUND);

        return PageableExecutionUtils.getPage(content,PageRequest.of(pageIndex,pageSize), ()->count.fetchOne());
    }

    private BooleanExpression blockedMemberNotInForComment(Member member) {
        List<Member> blockedMember = getBlockedMember(member);

        return blockedMember.isEmpty() ? null : comment.member.notIn(blockedMember);
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteComment(Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_COMMENT_EXIST));

        if (!findComment.getMember().equals(member))
            throw new RecipeException(RecipeStatus.NOT_COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(RecipeStatus.NOT_MATCH_RECIPE);
        else{
            commentRepository.deleteById(commentId);
            findRecipe.updateComment(-1);
        }

        return commentRepository.existsById(recipeId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Comment updateComment(RecipeRequestDto.updateCommentDto request, Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_COMMENT_EXIST));

        if (!findComment.getMember().equals(member))
            throw new RecipeException(RecipeStatus.NOT_COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(RecipeStatus.NOT_MATCH_RECIPE);
        else{
            return findComment.updateContent(request.getComment());
        }
    }

    @Transactional(readOnly = false)
    @Override
    public Long reportComment(Long recipeId, Long commentId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(RecipeStatus.NO_COMMENT_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(CommonStatus.NO_REPORT_EXIST));

        if (findComment.getMember().equals(member))
            throw new RecipeException(RecipeStatus.COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(RecipeStatus.NOT_MATCH_RECIPE);
        else{
            ReportedComment mapping = RecipeConverter.toCommentReport(findReport, findComment, member);
            reportedCommentRepository.save(mapping);

            return findComment.getId();
        }
    }

    // 내가 좋아요 누른 레시피 목록 DTO 조회
    @Override
    @Transactional
    public RecipeResponseDto.RecipePageListDto getLikeRecipes(Integer page, Member member) {
        Page<Recipe> likesRecipes = likesRepository.findRecipeByMember(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return RecipeConverter.toPagingRecipeDtoList(likesRecipes, member);
    }

    // 내가 스크랩 누른 레시피 목록 DTO 조회
    @Override
    @Transactional
    public RecipeResponseDto.RecipePageListDto getScrapRecipes(Integer page, Member member) {
        Page<Recipe> scrapRecipes = scrapRepository.findRecipeByMember(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return RecipeConverter.toPagingRecipeDtoList(scrapRecipes, member);
    }

}
