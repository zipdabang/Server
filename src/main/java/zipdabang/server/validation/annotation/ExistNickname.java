package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.ExistMemberRequestBodyValidator;
import zipdabang.server.validation.validator.ExistNicknameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistNicknameValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistNickname {
    String message() default "닉네임이 이미 존재합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
