package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.MemberService;
import zipdabang.server.validation.annotation.ExistNickname;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExistNicknameValidator implements ConstraintValidator<ExistNickname, String> {

    private final MemberService memberService;

    @Override
    public void initialize(ExistNickname constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        Optional<Member> member = memberService.checkExistNickname(value);
        if (member.isPresent()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(CommonStatus.NICKNAME_EXIST.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
