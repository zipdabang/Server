package zipdabang.server.utils.dto;

import lombok.*;

public class OAuthJoin {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OAuthJoinDto{
        String accessToken;
        String refreshToken;
    }
}
