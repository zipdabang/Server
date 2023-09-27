package zipdabang.server.validation.validator;

import zipdabang.server.base.Code;
import zipdabang.server.domain.member.Deregister;
import zipdabang.server.domain.member.Member;
import zipdabang.server.validation.annotation.CheckDeregister;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckDeregisterValidator implements ConstraintValidator<CheckDeregister, Object> {

    @Override
    public void initialize(CheckDeregister constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof Member) {
            Member member = (Member) value;
            if (member.getZipCode()==null) {
                return true;
            }
            if (member.getZipCode().equals("TEST")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(Code.DEREGISTER_FAIL.toString()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
