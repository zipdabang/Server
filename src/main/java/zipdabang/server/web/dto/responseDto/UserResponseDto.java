package zipdabang.server.web.dto.responseDto;

import lombok.*;



import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserResponseDto {


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class JoinUserDto {
        private Long userId;
        private String nickname;
        private String accessToken;
    }
    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserProfileDto {

        //        private String name;
        private String nickname;
        private String email;
        private String phoneNum;
        private String profileUrl;
    }


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class userStatusDto{
        private Long userId;
        private LocalDateTime calledAt;
    }
}
