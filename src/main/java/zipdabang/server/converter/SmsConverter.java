package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.sms.dto.SmsResponseDto;

@Component
@RequiredArgsConstructor
public class SmsConverter {

    public static SmsResponseDto.AuthNumResultDto toAuthNumResultDto(CommonStatus responseCommonStatus) {
        return SmsResponseDto.AuthNumResultDto.builder()
                .responseCommonStatus(responseCommonStatus)
                .build();
    }
}
