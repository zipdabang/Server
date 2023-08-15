package zipdabang.server.utils.dto;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthResult {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OAuthResultDto{
        Boolean isLogin;
        String accessToken;
        String refreshToken;
        Long memberId;
    }
}
