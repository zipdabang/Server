package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.sms.dto.SmsResponseDto;

@Component
@RequiredArgsConstructor
public class SmsConverter {

    public static SmsResponseDto.AuthNumResultDto toAuthNumResultDto(Code responseCode) {
        return SmsResponseDto.AuthNumResultDto.builder()
                .responseCode(responseCode)
                .build();
    }
}
