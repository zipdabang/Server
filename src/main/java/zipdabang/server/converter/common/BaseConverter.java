package zipdabang.server.converter.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.web.dto.common.BaseDto;

@Component
@RequiredArgsConstructor
public class BaseConverter {

    public static BaseDto.BaseResponseDto toBaseDto(Code responseCode, Object result) {
        return BaseDto.BaseResponseDto.builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build();
    }
}
