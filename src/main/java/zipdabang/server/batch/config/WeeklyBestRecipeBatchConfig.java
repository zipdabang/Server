package zipdabang.server.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.WeeklyBestRecipe;
import zipdabang.server.repository.recipeRepositories.RecipeRepository;
import zipdabang.server.repository.recipeRepositories.WeeklyBestRecipeRepository;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class WeeklyBestRecipeBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final RecipeRepository recipeRepository;
    private final WeeklyBestRecipeRepository weeklyBestRecipeRepository;

    @Value("10")
    private Integer chunkSize;

    @Bean
    public Job job() {
        Job job = jobBuilderFactory.get("WeeklyBestRecipeJob")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();

        return job;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("WeeklyBestRecipeStep1")
                .<WeeklyBestRecipe, WeeklyBestRecipe>chunk(5)
                .reader(step1ItemReader())
                .writer(step1ItemWriter())
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("WeeklyBestRecipeStep2")
                .<Recipe, WeeklyBestRecipe>chunk(chunkSize)
                .reader(step2ItemReader())
                .processor(step2ItemProcessor())
                .writer(step2ItemWriter())
                .build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("WeeklyBestRecipeStep3")
                .<Recipe, Recipe>chunk(chunkSize)
                .reader(step3ItemReader())
                .writer(step3ItemWriter())
                .build();
    }

    @StepScope
    @Bean
    public ListItemReader<WeeklyBestRecipe> step1ItemReader() {
        return new ListItemReader<WeeklyBestRecipe>(weeklyBestRecipeRepository.findAll());
    }

    @StepScope
    @Bean
    public ItemWriter<WeeklyBestRecipe> step1ItemWriter() {
        return bestRecipes -> bestRecipes.forEach(bestRecipe -> weeklyBestRecipeRepository.delete(bestRecipe));
    }

    @StepScope
    @Bean
    public ListItemReader<Recipe> step2ItemReader() {
        return new ListItemReader<Recipe>(recipeRepository.findTop5ByOrderByWeekLikeDescWeekScrapDescTotalLikeDescTotalScrapDesc());
    }

    @StepScope
    @Bean
    public ItemProcessor<Recipe, WeeklyBestRecipe> step2ItemProcessor() {
        AtomicInteger index = new AtomicInteger(1);

       return recipe -> WeeklyBestRecipe.builder()
                .ranking(index.getAndIncrement())
                .recipe(recipe)
                .build();
    }

    @StepScope
    @Bean
    public ItemWriter<WeeklyBestRecipe> step2ItemWriter() {
        return bestRecipes -> bestRecipes.forEach(bestRecipe -> weeklyBestRecipeRepository.save(bestRecipe));
    }

    @StepScope
    @Bean
    public ListItemReader<Recipe> step3ItemReader() {
        return new ListItemReader<Recipe>(recipeRepository.findAll());
    }

    @StepScope
    @Bean
    public ItemWriter<Recipe> step3ItemWriter() {
        return recipes -> recipes.forEach(recipe -> recipeRepository.updateWeeklyData(recipe));
    }
}
