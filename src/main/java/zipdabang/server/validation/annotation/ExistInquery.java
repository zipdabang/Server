package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.ExistInqueryValidator;
import zipdabang.server.validation.validator.ExistNicknameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistInqueryValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistInquery {
    String message() default "해당하는 문의가 존재하지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
