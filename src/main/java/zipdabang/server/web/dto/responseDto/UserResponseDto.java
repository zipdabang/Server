package zipdabang.server.web.dto.responseDto;

import lombok.*;

import javax.persistence.Column;

public class UserResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserProfileDto{

//        private String name;
        private String nickname;
        private String email;
        private String phoneNum;
        private String profileUrl;
    }
}
