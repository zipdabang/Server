package zipdabang.server.FeignClient.converter;

import zipdabang.server.FeignClient.dto.KakaoSocialUserDto;
import zipdabang.server.FeignClient.dto.OAuthInfoDto;

public class KakaoOAuthConverter {

    public static OAuthInfoDto toOAuthInfoDto(KakaoSocialUserDto kakaoSocialUserDto){
        return OAuthInfoDto.builder()
                .email(kakaoSocialUserDto.getKakao_account().getEmail())
                .profileUrl(kakaoSocialUserDto.getKakao_account().getProfile().getProfile_image_url())
                .build();
    }
}
