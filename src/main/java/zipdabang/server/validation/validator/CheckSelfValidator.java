package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.MemberException;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.MemberService;
import zipdabang.server.validation.annotation.CheckSelfTarget;
import zipdabang.server.validation.annotation.CheckTempMember;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class CheckSelfValidator implements ConstraintValidator<CheckSelfTarget, Long> {

    private final MemberService memberService;

    @Override
    public void initialize(CheckSelfTarget constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        Object principal = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            principal = authentication.getPrincipal();
        }
        if (principal == null || principal.getClass() == String.class) {
            throw new MemberException(CommonStatus.MEMBER_NOT_FOUND);
        }

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
        Long memberId = Long.valueOf(authentication.getName());
        if (value.equals(memberId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(CommonStatus.SELF_BANNED.toString()).addConstraintViolation();
            return false;
        }

        return true;
    }
}
