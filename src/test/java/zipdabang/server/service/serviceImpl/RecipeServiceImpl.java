package zipdabang.server.service.serviceImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.RecipeException;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.domain.test.TestRecipe;
import zipdabang.server.repository.recipeRepository.RecipeCategoryRepository;
import zipdabang.server.repository.testRepository.TestIngredientRepository;
import zipdabang.server.repository.testRepository.TestRecipeCategoryMappingRepository;
import zipdabang.server.repository.testRepository.TestRecipeRepository;
import zipdabang.server.repository.testRepository.TestStepRepository;
import zipdabang.server.repository.testRepository.testRecipeRepositoryCustom.TestRecipeRepositoryCustom;
import zipdabang.server.service.RecipeService;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final TestRecipeRepository testRecipeRepository;
    private final TestRecipeCategoryMappingRepository testRecipeCategoryMappingRepository;
    private final TestStepRepository testStepRepository;
    private final TestIngredientRepository testIngredientRepository;
    private final TestRecipeRepositoryCustom testRecipeRepositoryCustom;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final AmazonS3Manager amazonS3Manager;

    @Value("${paging.size}")
    Integer pageSize;

    @Override
    public RecipeCategory getRecipeCategory(Long categoryId) {
        return recipeCategoryRepository.findById(categoryId).get();
    }

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

        return new PageImpl<>(content, PageRequest.of(pageIndex,pageSize), count);
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
