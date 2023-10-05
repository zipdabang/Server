package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.domain.member.Member;
import zipdabang.server.validation.annotation.CheckTempMember;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class CheckTempMemberValidator implements ConstraintValidator<CheckTempMember, Object> {
    @Override
    public void initialize(CheckTempMember constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(value instanceof Member) {
            Member member = (Member)value;
            if(member.getMemberId().equals(0L)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(CommonStatus.TEMP_MEMBER_FORBIDDEN.toString()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
