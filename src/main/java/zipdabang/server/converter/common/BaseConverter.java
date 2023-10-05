package zipdabang.server.converter.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.web.dto.common.BaseDto;

@Component
@RequiredArgsConstructor
public class BaseConverter {

    public static BaseDto.BaseResponseDto toBaseDto(CommonStatus responseCommonStatus, Object result) {
        return BaseDto.BaseResponseDto.builder()
                .code(responseCommonStatus.getCode())
                .message(responseCommonStatus.getMessage())
                .build();
    }
}
