package zipdabang.server.utils.dto;

import lombok.*;
import zipdabang.server.domain.member.Terms;

import java.util.List;

public class OAuthJoin {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OAuthJoinDto{
        String accessToken;
        String refreshToken;
        List<Terms> termsList;
    }
}
