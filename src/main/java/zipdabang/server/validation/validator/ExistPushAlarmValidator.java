package zipdabang.server.validation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.handler.MemberException;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.service.MemberService;
import zipdabang.server.service.RootService;
import zipdabang.server.validation.annotation.ExistNotification;
import zipdabang.server.validation.annotation.ExistPushAlarm;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExistPushAlarmValidator implements ConstraintValidator<ExistPushAlarm,Long> {

    private final RootService rootService;

    private final MemberService memberService;

    @Override
    public void initialize(ExistPushAlarm constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        Optional<PushAlarm> pushAlarmById = rootService.findPushAlarmById(value);
        if (pushAlarmById.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(CommonStatus.PUSH_ALARM_NOT_FOUND.toString()).addConstraintViolation();
            return false;
        }
        else{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
            Long memberId = Long.valueOf(authentication.getName());
            if(!memberId.equals(pushAlarmById.get().getOwnerMember().getMemberId())){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(CommonStatus.NOT_MY_PUSH_ALARM.toString()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
