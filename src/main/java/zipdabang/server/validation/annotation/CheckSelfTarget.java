package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.CheckNicknameValidator;
import zipdabang.server.validation.validator.CheckSelfValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CheckSelfValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckSelfTarget {
    String message() default "다른 사람을 대상으로 해주세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
