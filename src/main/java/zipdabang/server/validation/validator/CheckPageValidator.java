package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.domain.member.Member;
import zipdabang.server.validation.annotation.CheckPage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class CheckPageValidator implements ConstraintValidator<CheckPage, Integer> {
    @Override
    public void initialize(CheckPage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if(value < 1 || value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Code.UNDER_PAGE_INDEX_ERROR.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
