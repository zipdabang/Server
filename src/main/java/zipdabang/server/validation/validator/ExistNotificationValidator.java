package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.repository.NotificationRepository;
import zipdabang.server.validation.annotation.ExistNotification;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExistNotificationValidator implements ConstraintValidator<ExistNotification,Long> {

    private final NotificationRepository notificationRepository;

    @Override
    public void initialize(ExistNotification constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        Optional<Notification> foundNotification = notificationRepository.findById(value);
        if(foundNotification.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Code.NOTIFICATION_NOT_FOUND.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
