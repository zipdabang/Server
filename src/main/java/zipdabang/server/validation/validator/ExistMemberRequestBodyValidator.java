package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.MemberService;
import zipdabang.server.validation.annotation.ExistMember;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExistMemberRequestBodyValidator implements ConstraintValidator<ExistMember, Long>{

    private final MemberService memberService;

    @Override
    public void initialize(ExistMember constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {

        Optional<Member> memberById = memberService.findMemberById(value);
        if(memberById.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Code.TARGET_MEMBER_NOT_FOUND.toString()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
