package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.RootService;
import zipdabang.server.validation.annotation.CheckNickname;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class CheckNicknameValidator implements ConstraintValidator<CheckNickname, Object>{
    private final RootService rootService;
    @Override
    public void initialize(CheckNickname constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof String) {
            String nickname = (String) value;
            if (rootService.isNicknameContainsSlangWord(nickname)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(CommonStatus.CONTAINS_SLANG_WORD.toString()).addConstraintViolation();
                return false;
            } else if (rootService.isNicknameReservedWord(nickname)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(CommonStatus.RESERVED_WORD.toString()).addConstraintViolation();
                return false;
            } else return true;
        }
        return true;
    }
}
