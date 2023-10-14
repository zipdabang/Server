package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.domain.member.Inquery;
import zipdabang.server.service.MemberService;
import zipdabang.server.validation.annotation.ExistInquery;
import zipdabang.server.validation.annotation.ExistNickname;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ExistInqueryValidator implements ConstraintValidator<ExistInquery, Long> {

    private final MemberService memberService;

    @Override
    public void initialize(ExistInquery constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (memberService.findInqueryById(value).isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(CommonStatus.INQUERY_NOT_FOUND.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
