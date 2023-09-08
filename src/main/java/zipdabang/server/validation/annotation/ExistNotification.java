package zipdabang.server.validation.annotation;

import zipdabang.server.validation.validator.CheckTempMemberValidator;
import zipdabang.server.validation.validator.ExistNotificationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistNotificationValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistNotification {
    String message() default "해당하는 게시글이 존재하지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
