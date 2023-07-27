package zipdabang.server.web.dto.responseDto;

import lombok.*;

public class UserResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserIdDto{
        private Long userId;
    }
}
