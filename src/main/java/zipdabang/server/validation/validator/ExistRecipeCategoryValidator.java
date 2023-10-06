package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.code.RecipeStatus;
import zipdabang.server.domain.recipe.RecipeCategory;
import zipdabang.server.repository.recipeRepositories.RecipeCategoryRepository;
import zipdabang.server.validation.annotation.ExistRecipeCategory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExistRecipeCategoryValidator implements ConstraintValidator<ExistRecipeCategory,Long> {

    private final RecipeCategoryRepository recipeCategoryRepository;

    @Override
    public void initialize(ExistRecipeCategory constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        Optional<RecipeCategory> findRecipeCategory = recipeCategoryRepository.findById(value);
        if(findRecipeCategory.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(RecipeStatus.NO_RECIPE_CATEGORY_EXIST.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
