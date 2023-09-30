package zipdabang.server.validation.annotation;


import zipdabang.server.validation.validator.ExistRecipeCategoryValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistRecipeCategoryValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistRecipeCategory {
    String message() default "범위에 없는 categoryId를 전달했습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
