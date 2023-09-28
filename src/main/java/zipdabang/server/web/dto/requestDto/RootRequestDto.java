package zipdabang.server.web.dto.requestDto;

import lombok.Getter;

public class RootRequestDto {

    @Getter
    public static class FCMTestDto{
        String fcmToken;
    }
}
