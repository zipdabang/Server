package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.CheckDeregisterValidator;
import zipdabang.server.validation.validator.CheckTempMemberValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CheckDeregisterValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckDeregister {
    String message() default "탈퇴가 불가능한 유저입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
