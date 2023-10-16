package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.ExistMemberRequestBodyValidator;
import zipdabang.server.validation.validator.ExistPushAlarmValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistPushAlarmValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistPushAlarm {

    String message() default "푸쉬알람이 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
