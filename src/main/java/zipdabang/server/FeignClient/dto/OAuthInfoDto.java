package zipdabang.server.FeignClient.dto;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthInfoDto {
    private String email;
    private String profileUrl;
}
