package zipdabang.server.service.serviceImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.MemberException;
import zipdabang.server.aws.s3.AmazonS3Manager;

import zipdabang.server.apiPayload.exception.handler.RecipeException;

import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.enums.AlarmType;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.member.*;
import zipdabang.server.domain.recipe.*;
import zipdabang.server.domain.test.QTestRecipe;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.firebase.fcm.service.FirebaseService;
import zipdabang.server.repository.AlarmRepository.AlarmCategoryRepository;
import zipdabang.server.repository.AlarmRepository.PushAlarmRepository;
import zipdabang.server.repository.ReportRepository;
import zipdabang.server.repository.memberRepositories.BlockedMemberRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.repository.recipeRepositories.*;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.CommentRepositoryCustom;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.RecipeRepositoryCustom;
import zipdabang.server.repository.recipeRepositories.recipeRepositoryCustom.TempRecipeRepositoryCustom;
import zipdabang.server.repository.testRepository.TestIngredientRepository;
import zipdabang.server.repository.testRepository.TestRecipeCategoryMappingRepository;
import zipdabang.server.repository.testRepository.TestRecipeRepository;
import zipdabang.server.repository.testRepository.TestStepRepository;
import zipdabang.server.repository.testRepository.testRecipeRepositoryCustom.TestRecipeRepositoryCustom;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeRepositoryCustom recipeRepositoryCustom;
    private final TempRecipeRepository tempRecipeRepository;
    private final TempRecipeRepositoryCustom tempRecipeRepositoryCustom;
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
    private final CommentRepositoryCustom commentRepositoryCustom;
    private final ReportRepository reportRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final ReportedRecipeRepository reportedRecipeRepository;
    private final WeeklyBestRecipeRepository weeklyBestRecipeRepository;

    private final PushAlarmRepository pushAlarmRepository;
    private final AlarmCategoryRepository alarmCategoryRepository;
    private final FirebaseService firebaseService;


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

        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        if(!recipe.getMember().equals(member))
            throw new RecipeException(CommonStatus.NOT_RECIPE_OWNER);

        recipeCategoryMappingRepository.deleteAllByRecipe(recipe);

        RecipeConverter.toRecipeCategory(request.getCategoryId(),recipe).stream()
                .map(categoryMapping -> recipeCategoryMappingRepository.save(categoryMapping))
                .collect(Collectors.toList())
                .stream()
                .map(categoryMapping -> categoryMapping.setRecipe(recipe));

        //recipe
        String deleteThumbnailUrl = null;
        String newThumbnailUrl = null;
        if (thumbnail != null) {
            deleteThumbnailUrl = recipe.getThumbnailUrl();
            newThumbnailUrl = RecipeConverter.uploadThumbnail(thumbnail);
            recipe.setThumbnail(newThumbnailUrl);
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


        //ingredient
        ingredientRepository.deleteAllByRecipe(recipe);

        RecipeConverter.toUpdateIngredient(request, recipe).stream()
                .map(ingredient -> ingredientRepository.save(ingredient))
                .collect(Collectors.toList())
                .stream()
                .map(ingredient -> ingredient.setRecipe(recipe));


        //s3 삭제는 맨 마지막에
        if (deleteThumbnailUrl != null)
            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(deleteThumbnailUrl).substring(1));

        if(!presentImageUrls.isEmpty())
            presentImageUrls.forEach(imageUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(imageUrl).substring(1)));


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

        TempRecipe tempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(CommonStatus.NO_TEMP_RECIPE_EXIST));

        //recipe
        String deleteThumbnailUrl = null;
        String newThumbnailUrl = null;
        if (thumbnail != null) {
            if (tempRecipe.getThumbnailUrl() != null)
                deleteThumbnailUrl = tempRecipe.getThumbnailUrl();
            newThumbnailUrl = RecipeConverter.uploadThumbnail(thumbnail);
        }
        else{
            if (request.getThumbnailUrl() == null && tempRecipe.getThumbnailUrl() != null)
                deleteThumbnailUrl = tempRecipe.getThumbnailUrl();
            else if (request.getThumbnailUrl() != null)
                newThumbnailUrl = request.getThumbnailUrl();
        }

        tempRecipe.setThumbnail(newThumbnailUrl);
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

        //s3 삭제는 맨 마지막에
        if (deleteThumbnailUrl != null)
            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(deleteThumbnailUrl).substring(1));
        if(!presentImageUrls.isEmpty())
            presentImageUrls.forEach(imageUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(imageUrl).substring(1)));

        return tempRecipe;

    }

    @Override
    public TempRecipe getTempRecipe(Long tempId) {
        return tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(CommonStatus.NO_TEMP_RECIPE_EXIST));
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean deleteTempRecipe(Long tempId, Member member) {

        TempRecipe findTempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(CommonStatus.NO_TEMP_RECIPE_EXIST));

        if (findTempRecipe.getMember().equals(member)) {
            String thumbnailUrl = null;
            List<String> stepUrls = new ArrayList<>();

            if(findTempRecipe.getThumbnailUrl() != null)
                thumbnailUrl=findTempRecipe.getThumbnailUrl();

            List<TempStep> tempSteps = tempStepRepository.findAllByTempRecipe(findTempRecipe);
            if (!tempSteps.isEmpty()) {
                    tempSteps.stream()
                        .forEach(step -> {
                                    if (step.getImageUrl() != null)
                                        stepUrls.add(step.getImageUrl());
                                }
                        );
            }
            tempRecipeRepository.deleteById(tempId);

            if (!thumbnailUrl.isEmpty())
                amazonS3Manager.deleteFile(RecipeConverter.toKeyName(thumbnailUrl).substring(1));
            stepUrls.stream()
                            .forEach(stepUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(stepUrl).substring(1)));
        }
        else
            throw new RecipeException(CommonStatus.NOT_RECIPE_OWNER);

        return tempRecipeRepository.existsById(tempId) == false;
    }

    @Override
    @Transactional(readOnly = false)
    public Recipe createFromTempRecipe(Long tempId, RecipeRequestDto.RecipeCategoryList categoryList, Member member) {
        TempRecipe tempRecipe = tempRecipeRepository.findById(tempId).orElseThrow(() -> new RecipeException(CommonStatus.NO_TEMP_RECIPE_EXIST));
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

        List<TempRecipe> content = tempRecipeRepositoryCustom.getTempRecipePageList(pageIndex, pageSize, member);

        Long count =tempRecipeRepositoryCustom.getTempRecipeTotalCount(member);

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count);
    }

    @Transactional(readOnly = false)
    @Override
    public Recipe getRecipe(Long recipeId, Member member) {

        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        Optional<BlockedMember> blockedInfos= blockedMemberRepository.findByOwnerAndBlocked(member, findRecipe.getMember());

        if(blockedInfos.isPresent()){
            throw new RecipeException(CommonStatus.BLOCKED_USER_RECIPE);
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
    public Page<Recipe> searchRecipe(Long categoryId, String keyword, String order, Integer pageIndex, Member member) {

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        List<Recipe> content = new ArrayList<>();

        BooleanExpression categoryCondition = recipeRepositoryCustom.recipesInCategoryCondition(categoryId);
        BooleanExpression keywordCondition = recipeRepositoryCustom.recipesContainKeyword(keyword);

        if(order.equals("follow"))
            content = recipeRepositoryCustom.recipesOrderByFollow(pageIndex, pageSize, member, categoryCondition, keywordCondition);
        else
            content = recipeRepositoryCustom.recipesOrderBy(pageIndex,pageSize, member, order, categoryCondition, keywordCondition);

        Long count = recipeRepositoryCustom.recipeTotalCount(member, categoryCondition, keywordCondition);

        if (count < pageIndex*pageSize)
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);
        if (content.size() > count - pageIndex*pageSize)
            content = content.subList(0, count.intValue()-pageIndex*pageSize);

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count);
    }

    @Override
    public Long searchRecipeCounting(Long categoryId, String keyword, Member member) {

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        BooleanExpression categoryCondition = recipeRepositoryCustom.recipesInCategoryCondition(categoryId);

        BooleanExpression keywordCondition = recipeRepositoryCustom.recipesContainKeyword(keyword);
        Long count = recipeRepositoryCustom.recipeTotalCount(member, categoryCondition, keywordCondition);

        return count;
    }

    @Override
    public List<Recipe> getWrittenByRecipePreview(String writtenby, Member member) {

        List<Recipe> recipeList = recipeRepositoryCustom.getWrittenByPreview(writtenby, member, pageSize);

        log.info(recipeList.toString());

        return recipeList;
    }

    @Override
    public Page<Recipe> getWrittenByRecipe(Integer pageIndex, String writtenby, String order, Member member) {

        List<Recipe> content = new ArrayList<>();

        BooleanExpression writtenByCondition = recipeRepositoryCustom.checkWrittenBy(writtenby);

        if(order.equals("follow"))
            content = recipeRepositoryCustom.recipesOrderByFollow(pageIndex, pageSize, member, writtenByCondition);
        else
            content = recipeRepositoryCustom.recipesOrderBy(pageIndex,pageSize, member, order, writtenByCondition);

        Long count = recipeRepositoryCustom.recipeTotalCount(member, writtenByCondition);

        if (count < pageIndex*pageSize)
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);
        if (content.size() != 0 & content.size() > count - pageIndex*pageSize)
            content = content.subList(0, count.intValue()-pageIndex*pageSize);

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count);
    }

    @Override
    public Long getWrittenByRecipeCounting(String writtenby, Member member) {
        BooleanExpression writtenByCondition = recipeRepositoryCustom.checkWrittenBy(writtenby);

        Long count = recipeRepositoryCustom.recipeTotalCount(member, writtenByCondition);

        return count;
    }

    @Override
    @Transactional(readOnly = false)
    public Recipe updateLikeOnRecipe(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        if(recipe.getMember() == member)
            throw new RecipeException(CommonStatus.RECIPE_OWNER);

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
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        if(recipe.getMember() == member)
            throw new RecipeException(CommonStatus.RECIPE_OWNER);

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

        List<Recipe> recipeList = new ArrayList<>();

        recipeList = recipeRepositoryCustom.getTop5RecipePerCategory(categoryId);

        log.info(recipeList.toString());

        return recipeList;
    }

    @Transactional(readOnly = false)
    @Override
    public Page<Recipe> recipeListByCategory(Long categoryId, Integer pageIndex, Member member, String order) {

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        List<Recipe> content = new ArrayList<>();

        BooleanExpression whereCondition = recipeRepositoryCustom.recipesInCategoryCondition(categoryId);


        if(order.equals("follow"))
            content = recipeRepositoryCustom.recipesOrderByFollow(pageIndex, pageSize, member, whereCondition);
        else
            content = recipeRepositoryCustom.recipesOrderBy(pageIndex,pageSize, member, order, whereCondition);

        log.info("서비스단의 상황 : {}", content.size());
        Long count = recipeRepositoryCustom.recipeTotalCount(member, whereCondition);

        if (count < pageIndex*pageSize)
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);
        if (content.size() > count - pageIndex*pageSize)
            content = content.subList(0, count.intValue()-pageIndex*pageSize);

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count);
    }

    @Override
    public Long getrecipeListByCategoryCounting(Long categoryId, Member member) {

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        BooleanExpression whereCondition = recipeRepositoryCustom.recipesInCategoryCondition(categoryId);
        Long count = recipeRepositoryCustom.recipeTotalCount(member, whereCondition);

        return count;
    }

    @Override
    public boolean checkRecipeCategoryExist(Long categoryId) {
        return recipeCategoryRepository.existsById(categoryId);
    }

    @Override
    public List<List<Recipe>> searchRecipePreview(String keyword, Member member) {
        Long recipeCategorySize = recipeCategoryRepository.count()-1;

        List<Member> blockedMember = getBlockedMember(member);

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

    List<Member> getBlockedMember(Member member) {
        List<Member> blockedMember = blockedMemberRepository.findByOwner(member).stream()
                .map(blockedInfo -> blockedInfo.getBlocked())
                .collect(Collectors.toList());
        return blockedMember;
    }

    @Override
    public List<WeeklyBestRecipe> WeekBestRecipe() {
        List<WeeklyBestRecipe> bestRecipes = weeklyBestRecipeRepository.findAll();

        return bestRecipes;
    }

    @Override
    public List<Recipe> getRecipeByOwnerPreview(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));

        List<Recipe> recipeList = recipeRepository.findTop5ByMemberOrderByCreatedAtDesc(findMember);

        if(recipeList.size() == 0)
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        return recipeList;
    }

    @Override
    public Page<Recipe> getRecipeByOwner(Integer pageIndex, Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));

        Page<Recipe> recipeList = recipeRepository.findByMember(findMember, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        return recipeList;
    }

    @Override
    public List<Recipe> getMyRecipePreview(Member member) {
        List<Recipe> recipeList = recipeRepository.findTop5ByMemberOrderByCreatedAtDesc(member);

        if(recipeList.size() == 0)
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        return recipeList;
    }

    @Override
    public Page<Recipe> getMyRecipeList(Integer pageIndex, Member member) {
        Page<Recipe> recipeList = recipeRepository.findByMember(member, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        return recipeList;
    }

    public List<RecipeBanner> getRecipeBannerList() {
        return recipeBannerRepository.findAll();
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteRecipe(Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        if (findRecipe.getMember().equals(member)) {

            if (weeklyBestRecipeRepository.existsByRecipe(findRecipe))
                    weeklyBestRecipeRepository.deleteByRecipe(findRecipe);

            String thumbnailUrl = findRecipe.getThumbnailUrl();
            List<String> stepUrlList = stepRepository.findAllByRecipeId(recipeId).stream()
                    .filter(steps -> steps.getImageUrl() != null)
                    .map(step -> step.getImageUrl())
                    .collect(Collectors.toList());

            recipeRepository.deleteById(recipeId);

            amazonS3Manager.deleteFile(RecipeConverter.toKeyName(thumbnailUrl).substring(1));
            stepUrlList
                    .forEach(stepUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(stepUrl).substring(1)));
        }
        else
            throw new RecipeException(CommonStatus.NOT_RECIPE_OWNER);

        return recipeRepository.existsById(recipeId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Long reportRecipe(Long recipeId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(CommonStatus.NO_REPORT_EXIST));

        if (!findRecipe.getMember().equals(member)) {
            ReportedRecipe mapping = RecipeConverter.toRecipeReport(findReport, findRecipe, member);
            reportedRecipeRepository.save(mapping);

            return findRecipe.getId();
        }
        else
            throw new RecipeException(CommonStatus.RECIPE_OWNER);
    }

    @Transactional(readOnly = false)
    @Override
    public Comment createComment(String content, Long recipeId, Member member){
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        findRecipe.updateComment(1);

        if (!member.equals(findRecipe.getMember())) {

            List<PushAlarm> existAlarms = pushAlarmRepository.findByTitleAndOwnerMemberAndIsConfirmedFalse("나의 글에 댓글이 달렸어요. 확인해보세요!", findRecipe.getMember());
            Boolean isMoreThan5Comments = !pushAlarmRepository.findByTitleAndOwnerMemberAndIsConfirmedFalse("확인하지 않은 댓글 알림이 5개 이상 있어요.", findRecipe.getMember()).isEmpty();

            String title = "나의 글에 댓글이 달렸어요. 확인해보세요!";
            String body = content;
            String targetView = AlarmType.RECIPE.toString();
            String targetPK = findRecipe.getId().toString();

            PushAlarm pushAlarm = pushAlarmRepository.save(PushAlarm.builder()
                    .title(title)
                    .body(body)
                    .isConfirmed(false)
                    .targetRecipe(findRecipe)
                    .alarmCategory(alarmCategoryRepository.findByName(AlarmType.RECIPE).get())
                    .build());

            pushAlarm.setMember(findRecipe.getMember());
            pushAlarm.setRecipe(findRecipe);

            if (!isMoreThan5Comments) {

                if (existAlarms.size() == 4) {

                    title = "확인하지 않은 댓글 알림이 5개 이상 있어요.";
                    body = "확인해보세요!";
                    targetView = AlarmType.ALARMPAGE.toString();
                    targetPK = findRecipe.getMember().getMemberId().toString();


                    pushAlarm = pushAlarmRepository.save(PushAlarm.builder()
                            .title(title)
                            .body(body)
                            .isConfirmed(false)
                            .alarmCategory(alarmCategoryRepository.findByName(AlarmType.ALARMPAGE).get())
                            .build());

                    pushAlarm.setMember(findRecipe.getMember());
                    pushAlarm.setRecipe(findRecipe);
                }

                for (FcmToken fcmToken : findRecipe.getMember().getFcmTokenList()) {
                    try {
                        firebaseService.sendMessageTo(fcmToken.getToken(), title, body, targetView, targetPK, pushAlarm.getId().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        Comment buildComment = RecipeConverter.toComment(content, findRecipe, member);
        return commentRepository.save(buildComment);
    }

    @Override
    public Page<Comment> commentList(Integer pageIndex, Long recipeId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        List<Comment> content = commentRepositoryCustom.getCommentList(pageIndex, pageSize, member, findRecipe);


        Long count = commentRepositoryCustom.commentListTotalCount(member,findRecipe);

        if (count == 0)
            throw new RecipeException(CommonStatus.COMMENT_NOT_FOUND);

        return PageableExecutionUtils.getPage(content,PageRequest.of(pageIndex,pageSize), ()->count);
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteComment(Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(CommonStatus.NO_COMMENT_EXIST));

        if (!findComment.getMember().equals(member))
            throw new RecipeException(CommonStatus.NOT_COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(CommonStatus.NOT_MATCH_RECIPE);
        else{
            commentRepository.deleteById(commentId);
            findRecipe.updateComment(-1);
        }

        return commentRepository.existsById(commentId) == false;
    }

    @Transactional(readOnly = false)
    @Override
    public Comment updateComment(RecipeRequestDto.updateCommentDto request, Long recipeId, Long commentId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(CommonStatus.NO_COMMENT_EXIST));

        if (!findComment.getMember().equals(member))
            throw new RecipeException(CommonStatus.NOT_COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(CommonStatus.NOT_MATCH_RECIPE);
        else{
            return findComment.updateContent(request.getComment());
        }
    }

    @Override
    public Long getCommentCount(Recipe recipe, Member member) {
        return commentRepositoryCustom.commentListTotalCount(member, recipe);
    }

    @Transactional(readOnly = false)
    @Override
    public Long reportComment(Long recipeId, Long commentId, Long reportId, Member member) {
        Recipe findRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeException(CommonStatus.NO_RECIPE_EXIST));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new RecipeException(CommonStatus.NO_COMMENT_EXIST));
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new RecipeException(CommonStatus.NO_REPORT_EXIST));

        if (findComment.getMember().equals(member))
            throw new RecipeException(CommonStatus.COMMENT_OWNER);
        else if (!findComment.getRecipe().equals(findRecipe))
            throw new RecipeException(CommonStatus.NOT_MATCH_RECIPE);
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

    @Override
    public Boolean checkOwnerBlocked(Recipe recipe, Member member) {
        return blockedMemberRepository.existsByOwnerAndBlocked(member, recipe.getMember());
    }

    @Override
    public Boolean checkIsLiked(Recipe recipe, Member member) {
        return likesRepository.findByRecipeAndMember(recipe, member).isPresent();
    }

    @Override
    public Boolean checkIsScrapped(Recipe recipe, Member member) {
        return scrapRepository.findByRecipeAndMember(recipe,member).isPresent();
    }

    @Override
    public RecipeCategory getRecipeCategory(Long categoryId) {
        return recipeCategoryRepository.findById(categoryId).get();
    }

    /**
     * 부하테스트용 서비스
     */
    private final TestRecipeRepository testRecipeRepository;
    private final TestRecipeCategoryMappingRepository testRecipeCategoryMappingRepository;
    private final TestStepRepository testStepRepository;
    private final TestIngredientRepository testIngredientRepository;
    private final TestRecipeRepositoryCustom testRecipeRepositoryCustom;

    @Override
    @Transactional(readOnly = false)
    public TestRecipe testCreate(RecipeRequestDto.CreateRecipeDto request, MultipartFile thumbnail, List<MultipartFile> stepImages) throws IOException {

        CompletableFuture<TestRecipe> savedRecipeFuture = CompletableFuture.supplyAsync(() ->{
            TestRecipe buildRecipe = null;
            try {
                buildRecipe = RecipeConverter.toTestRecipe(request, thumbnail);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return testRecipeRepository.save(buildRecipe);
        });

        savedRecipeFuture.thenAccept(recipe -> {
            RecipeConverter.toTestRecipeCategory(request.getCategoryId(),recipe).join().stream()
                    .map(categoryMapping -> testRecipeCategoryMappingRepository.save(categoryMapping))
                    .collect(Collectors.toList())
                    .stream()
                    .map(categoryMapping -> categoryMapping.setRecipe(recipe));
        });


        savedRecipeFuture.thenAccept(recipe -> {
            RecipeConverter.toTestStep(request, recipe, stepImages).join().stream()
                    .map(step -> testStepRepository.save(step))
                    .collect(Collectors.toList())
                    .stream()
                    .map(step -> step.setRecipe(recipe));
        });

        savedRecipeFuture.thenAccept(recipe -> {
            RecipeConverter.toTestIngredient(request, recipe).join().stream()
                    .map(ingredient -> testIngredientRepository.save(ingredient))
                    .collect(Collectors.toList())
                    .stream()
                    .map(ingredient -> ingredient.setRecipe(recipe));
        });

        return savedRecipeFuture.join();
    }

    @Override
    @Transactional(readOnly = false)
    public TestRecipe testCreateWithImageUrl(RecipeRequestDto.CreateRecipeWithImageUrlDto request){
        TestRecipe buildRecipe = RecipeConverter.toTestRecipeWithImageUrl(request);
        testRecipeRepository.save(buildRecipe);

        RecipeConverter.toTestRecipeCategory(request.getCategoryId(),buildRecipe).join().stream()
                .map(categoryMapping -> testRecipeCategoryMappingRepository.save(categoryMapping))
                .collect(Collectors.toList())
                .stream()
                .map(categoryMapping -> categoryMapping.setRecipe(buildRecipe));

        RecipeConverter.toTestStepWithImageUrl(request, buildRecipe).join().stream()
                .map(step -> testStepRepository.save(step))
                .collect(Collectors.toList())
                .stream()
                .map(step -> step.setRecipe(buildRecipe));

        RecipeConverter.toTestIngredientWithImageUrl(request, buildRecipe).join().stream()
                .map(ingredient -> testIngredientRepository.save(ingredient))
                .collect(Collectors.toList())
                .stream()
                .map(ingredient -> ingredient.setRecipe(buildRecipe));

        return buildRecipe;
    }

    @Override
    public TestRecipe getTestRecipe(Long recipeId) {
        TestRecipe findRecipe = testRecipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(CommonStatus.NO_RECIPE_EXIST));

        findRecipe.updateView();
        return findRecipe;
    }

    @Transactional
    @Override
    public Page<TestRecipe> testRecipeListByCategory(Long categoryId, Integer pageIndex, String order) {

        List<RecipeCategory> recipeCategory = recipeCategoryRepository.findAllById(categoryId);

        if(recipeCategory.isEmpty())
            throw new RecipeException(CommonStatus.RECIPE_NOT_FOUND);

        List<TestRecipe> content = new ArrayList<>();

        BooleanExpression whereCondition = testRecipeRepositoryCustom.recipesInCategoryCondition(categoryId);


        content = testRecipeRepositoryCustom.testRecipesOrderBy(pageIndex,pageSize, order, whereCondition);

        log.info("서비스단의 상황 : {}", content.size());
        Long count = testRecipeRepositoryCustom.testRecipeTotalCount(whereCondition);

        if (count < pageIndex*pageSize)
            throw new RecipeException(CommonStatus.OVER_PAGE_INDEX_ERROR);
        if (content.size() > count - pageIndex*pageSize)
            content = content.subList(0, count.intValue()-pageIndex*pageSize);

        return new PageImpl<>(content,PageRequest.of(pageIndex,pageSize), count);
    }

    @Transactional(readOnly = false)
    @Override
    public Boolean deleteTestRecipe() {
        List<TestRecipe> findRecipes = testRecipeRepository.findAll();

        List<String> thumbnailUrls = findRecipes.stream().parallel()
            .map(recipe -> recipe.getThumbnailUrl()).collect(Collectors.toList());
        List<String> stepUrlList = testStepRepository.findAll().stream().parallel()
                .filter(steps -> steps.getImageUrl() != null)
                .map(step -> step.getImageUrl())
                .collect(Collectors.toList());

        testRecipeRepository.deleteAll();

        thumbnailUrls.parallelStream().forEach(thumbnailUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(thumbnailUrl).substring(1)));
        stepUrlList.parallelStream()
                .forEach(stepUrl -> amazonS3Manager.deleteFile(RecipeConverter.toKeyName(stepUrl).substring(1)));

        return testRecipeRepository.count() == 0;

    }
}
